package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.operations.NukeOperation.NukeResult;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.file.PathUtils;
import org.graalvm.collections.Pair;
import org.graalvm.nativeimage.ImageInfo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class NukeOperation implements Operation<NukeResult> {
    private final CliContext ctx;
    private final NukeRequest request;

    public record NukeRequest(
        boolean dryRun,
        Optional<Boolean> preserveAstrarc,
        Optional<String> cliName,
        boolean yes,
        BiFunction<Path, Boolean, Boolean> promptShouldDeleteAstrarc,
        Runnable assertShouldNuke
    ) {}

    public sealed interface NukeResult {}
    public record Nuked(Set<Path> deletedFiles, Set<Path> updatedFiles, Map<Path, SkipReason> skipped, Optional<String> finalDeleteCmd) implements NukeResult {}
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
        if (!request.dryRun && !request.yes) {
            request.assertShouldNuke.run();
        }

        val cliBinary = resolveCliBinary();

        val cliName = cliBinary
            .map(PathUtils::getFileNameString)
            .or(() -> request.cliName);

        if (cliName.isEmpty()) {
            return new CouldNotResolveCliName();
        }

        val astraHome = resolveAstraHome();
        val astraRc = resolveAstraRc();
        val rcFiles = resolveRcFilesWithAutocomplete(cliName.get());

        val processRunningFromInsideAstraHome = cliBinary.isPresent() && cliBinary.get().startsWith(astraHome);

        val shouldPreserveAstrarcAstraRc = request.preserveAstrarc.orElseGet(() -> {
            if (!Files.exists(astraRc)) {
                return false;
            }
            return !request.promptShouldDeleteAstrarc.apply(astraRc, request.dryRun);
        });

        val res = mkResult(cliBinary, processRunningFromInsideAstraHome, astraHome);

        if (!shouldPreserveAstrarcAstraRc) {
            delete(astraRc, res);
        }

        try {
            @Cleanup val astraHomeFiles = Files.list(astraHome);

            if (Files.exists(astraHome)) {
                if (processRunningFromInsideAstraHome) {
                    astraHomeFiles.filter(f -> f.endsWith("/cli")).forEach((f) -> delete(f, res));
                } else {
                    delete(astraHome, res);
                }
            } else {
                res.skipped().put(astraHome, new SkipReason.NotFound("delete"));
            }
        } catch (IOException e) {
            res.skipped().put(astraHome, new SkipReason.JustCouldNot("delete", e.getMessage()));
        }

        rcFiles.forEach((pair) -> {
            update(pair.getLeft(), pair.getRight(), res);
        });

        return res;
    }

    private @NotNull Nuked mkResult(Optional<Path> maybeBinaryFile, boolean processRunningFromInsideAstraHome, Path astraHome) {
        val finalDeleteCmd = maybeBinaryFile.map((binaryFile) -> (
            (!ctx.isWindows())
                ? (processRunningFromInsideAstraHome)
                    ? "rm -rf " + astraHome
                    : "rm " + binaryFile
                : (processRunningFromInsideAstraHome)
                    ? "rmdir /s /q " + astraHome
                    : "del " + binaryFile
        ));

        return new Nuked(new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashMap<>(), finalDeleteCmd);
    }

    private Optional<Path> resolveCliBinary() {
        val file = ProcessHandle.current()
            .info()
            .command()
            .map(ctx::path);

        if (file.isEmpty() || !ImageInfo.inImageCode()) {
            return Optional.empty();
        }

        return file;
    }

    private Path resolveAstraHome() {
        return ctx.home().DIR;
    }

    private Path resolveAstraRc() {
        return AstraConfig.resolveDefaultAstraConfigFile(ctx);
    }

    private Set<Pair<Path, String>> resolveRcFilesWithAutocomplete(String cliName) {
        if (ctx.isWindows()) {
            return Set.of();
        }

        val autocompletePattern = Pattern.compile(
            "^.*(?:source|\\.)\\s+<\\(\\s*" + cliName + "\\s+compgen(?:\\s+[^)]*)?\\s*\\)\\s*$",
            Pattern.MULTILINE
        );

        return Stream.of(".bashrc", ".zshrc", ".profile", ".bash_profile", ".zprofile")
            .map(name -> ctx.path(System.getProperty("user.home"), name))
            .filter(Files::exists)
            .flatMap((f) -> {
                return Either
                    .tryCatch(() -> Files.readString(f), (e) -> {
                        ctx.log().warn("Could not read file '", f.toString(), "' to check for any astra-cli autocomplete entries: ", e.getMessage());
                        return null;
                    })
                    .map((content) -> {
                        val matcher = autocompletePattern.matcher(content);

                        return (matcher.find())
                            ? Stream.of(Pair.create(f, matcher.replaceAll("")))
                            : Stream.<Pair<Path, String>>of();
                    })
                    .fold(_ -> Stream.of(), r -> r);
            })
            .collect(Collectors.toSet());
    }

    private void delete(Path path, Nuked res) {
        if (failsPreliminaryChecks(path, res, "delete")) {
            return;
        }

        try {
            if (!request.dryRun) {
                if (Files.isDirectory(path)) {
                    PathUtils.deleteDirectory(path);
                } else {
                    PathUtils.delete(path);
                }
            }
            res.deletedFiles().add(path);
        } catch (Exception e) {
            res.skipped().put(path, new SkipReason.JustCouldNot("delete", e.getMessage()));
        }
    }

    private void update(Path path, String newContent, Nuked res) {
        if (failsPreliminaryChecks(path, res, "update")) {
            return;
        }

        try {
            if (!request.dryRun) {
                Files.writeString(path, newContent);
            }
            res.updatedFiles().add(path);
        } catch (IOException e) {
            res.skipped().put(path, new SkipReason.JustCouldNot("update", e.getMessage()));
        }
    }

    private boolean failsPreliminaryChecks(Path path, Nuked res, String operation) {
        if (!Files.exists(path)) {
            res.skipped().put(path, new SkipReason.NotFound(operation));
            return true;
        }

        if (needsSudo(path)) {
            res.skipped().put(path, new SkipReason.NeedsSudo(operation));
            return true;
        }

        return false;
    }

    private boolean needsSudo(Path path) {
        try {
            return !Files.isWritable(path);
        } catch (Exception e) {
            ctx.log().warn("Could not determine if file '", path.toString(), "' needs sudo: ", e.getMessage());
            return false;
        }
    }
}
