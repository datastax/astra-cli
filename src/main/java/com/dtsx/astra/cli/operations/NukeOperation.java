package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.AstraHome;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.operations.NukeOperation.NukeResult;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.graalvm.collections.Pair;
import org.graalvm.nativeimage.ImageInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.MiscUtils.isWindows;

@RequiredArgsConstructor
public class NukeOperation implements Operation<NukeResult> {
    private final NukeRequest request;

    public record NukeRequest(
        boolean dryRun,
        Optional<Boolean> preserveAstrarc,
        Optional<String> cliName,
        BiFunction<File, Boolean, Boolean> promptShouldDeleteAstrarc
    ) {}

    public sealed interface NukeResult {}
    public record Nuked(Set<File> deletedFiles, Set<File> updatedFiles, Map<File, SkipReason> skipped, Optional<String> finalDeleteCmd) implements NukeResult {}
    public record CouldNotResolveCliName() implements NukeResult {}

    public sealed interface SkipReason {
        String reason();

        record NeedsSudo(String operation) implements SkipReason {
            @Override
            public String reason() {
                return "Needs higher permissions to " + operation;
            }
        }

        record NotFound(String operation) implements SkipReason {
            @Override
            public String reason() {
                return "Could not find file to " + operation;
            }
        }

        record JustCouldNot(String operation, String reason) implements SkipReason {
            @Override
            public String reason() {
                return "Could not " + operation + " file: " + reason;
            }
        }
    }

    @Override
    public NukeResult execute() {
        val cliBinary = resolveCliBinary();

        val cliName = cliBinary
            .map(File::getName)
            .or(() -> request.cliName);

        if (cliName.isEmpty()) {
            return new CouldNotResolveCliName();
        }

        val astraHome = resolveAstraHome();
        val astraRc = resolveAstraRc();
        val rcFiles = resolveRcFilesWithAutocomplete(cliName.get());

        val processRunningFromInsideAstraHome = cliBinary.isPresent() && cliBinary.get().toPath().startsWith(astraHome.toPath());

        val shouldPreserveAstrarcAstraRc = request.preserveAstrarc.orElseGet(() -> {
            if (!astraRc.exists()) {
                return false;
            }
            return !request.promptShouldDeleteAstrarc.apply(astraRc, request.dryRun);
        });

        val res = mkResult(cliBinary, processRunningFromInsideAstraHome, astraHome);

        if (!shouldPreserveAstrarcAstraRc) {
            delete(astraRc, res);
        }

        val astraHomeFiles = astraHome.listFiles();

        if (Files.exists(astraHome.toPath()) && astraHomeFiles != null) {
            if (processRunningFromInsideAstraHome) {
                Arrays.stream(astraHomeFiles).filter(f -> f.toPath().endsWith("/cli")).forEach((f) -> delete(f, res));
            } else {
                delete(astraHome, res);
            }
        } else {
            res.skipped().put(astraHome, new SkipReason.NotFound("delete"));
        }

        rcFiles.forEach((pair) -> {
            update(pair.getLeft(), pair.getRight(), res);
        });

        return res;
    }

    private static @NotNull Nuked mkResult(Optional<File> maybeBinaryFile, boolean processRunningFromInsideAstraHome, File astraHome) {
        val finalDeleteCmd = maybeBinaryFile.map((binaryFile) -> (
            (!isWindows())
                ? (processRunningFromInsideAstraHome)
                    ? "rm -rf " + astraHome.getAbsolutePath()
                    : "rm " + binaryFile.getAbsolutePath()
                : (processRunningFromInsideAstraHome)
                    ? "rmdir /s /q " + astraHome.getAbsolutePath()
                    : "del " + binaryFile.getAbsolutePath()
        ));

        return new Nuked(new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashMap<>(), finalDeleteCmd);
    }

    private Optional<File> resolveCliBinary() {
        val file = ProcessHandle.current()
            .info()
            .command()
            .map(File::new);

        if (file.isEmpty() || !ImageInfo.inImageCode()) {
            return Optional.empty();
        }

        return file;
    }

    private File resolveAstraHome() {
        return AstraHome.DIR;
    }

    private File resolveAstraRc() {
        return AstraConfig.resolveDefaultAstraConfigFile();
    }

    private Set<Pair<File, String>> resolveRcFilesWithAutocomplete(String cliName) {
        if (isWindows()) {
            return Set.of();
        }

        val autocompletePattern = Pattern.compile(
            "^.*(?:source|\\.)\\s+<\\(\\s*" + cliName + "\\s+compgen(?:\\s+[^)]*)?\\s*\\)\\s*$",
            Pattern.MULTILINE
        );

        return Stream.of(".bashrc", ".zshrc", ".profile", ".bash_profile", ".zprofile")
            .map(name -> new File(System.getProperty("user.home"), name))
            .filter(File::exists)
            .flatMap((f) -> {
                return Either
                    .tryCatch(() -> Files.readString(f.toPath()), (e) -> {
                        AstraLogger.warn("Could not read file '", f.toString(), "' to check for any astra-cli autocomplete entries: ", e.getMessage());
                        return null;
                    })
                    .map((content) -> {
                        val matcher = autocompletePattern.matcher(content);

                        return (matcher.find())
                            ? Stream.of(Pair.create(f, matcher.replaceAll("")))
                            : Stream.<Pair<File, String>>of();
                    })
                    .fold(_ -> Stream.of(), r -> r);
            })
            .collect(Collectors.toSet());
    }

    private void delete(File file, Nuked res) {
        if (failsPreliminaryChecks(file, res, "delete")) {
            return;
        }

        try {
            if (!request.dryRun) {
                if (file.isDirectory()) {
                    FileUtils.deleteDirectory(file);
                } else {
                    FileUtils.delete(file);
                }
            }
            res.deletedFiles().add(file);
        } catch (Exception e) {
            res.skipped().put(file, new SkipReason.JustCouldNot("delete", e.getMessage()));
        }
    }

    private void update(File file, String newContent, Nuked res) {
        if (failsPreliminaryChecks(file, res, "update")) {
            return;
        }

        try {
            if (!request.dryRun) {
                Files.writeString(file.toPath(), newContent);
            }
            res.updatedFiles().add(file);
        } catch (IOException e) {
            res.skipped().put(file, new SkipReason.JustCouldNot("update", e.getMessage()));
        }
    }

    private boolean failsPreliminaryChecks(File file, Nuked res, String operation) {
        if (!Files.exists(file.toPath())) {
            res.skipped().put(file, new SkipReason.NotFound(operation));
            return true;
        }

        if (needsSudo(file)) {
            res.skipped().put(file, new SkipReason.NeedsSudo(operation));
            return true;
        }

        return false;
    }

    private boolean needsSudo(File file) {
        try {
            return !Files.isWritable(file.toPath());
        } catch (Exception e) {
            AstraLogger.warn("Could not determine if file '", file.toString(), "' needs sudo: ", e.getMessage());
            return false;
        }
    }
}
