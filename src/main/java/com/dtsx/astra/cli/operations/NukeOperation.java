package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.operations.NukeOperation.NukeResult;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.io.file.PathUtils;
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

import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;

@RequiredArgsConstructor
public class NukeOperation implements Operation<NukeResult> {
    private final CliContext ctx;
    private final NukeRequest request;

    public record NukeRequest(
        boolean dryRun,
        Optional<Boolean> preserveAstrarc,
        boolean yes,
        BiFunction<List<Path>, Boolean, Boolean> promptShouldDeleteAstrarc,
        Runnable assertShouldNuke
    ) {}

    public sealed interface BinaryDeleteResult {}
    public record BinaryMustBeDeleted(String deleteCommand) implements BinaryDeleteResult {}
    public record BinaryNotWritable(Path path) implements BinaryDeleteResult {}
    public record BinaryDeleted() implements BinaryDeleteResult {}

    @Data
    @AllArgsConstructor
    @Accessors(fluent = true)
    public static class NukeResult {
        private Set<Path> deletedFiles;
        private Set<Path> shellRcFilesToUpdate;
        private Map<Path, SkipDeleteReason> skipped;
        private BinaryDeleteResult binaryDeleteResult;
        private Path cliBinaryPath;
    }

    public sealed interface SkipDeleteReason {
        String reason();

        record UserChoseToKeep() implements SkipDeleteReason {
            public String reason() {
                return "User chose to keep the file";
            }
        }

        record NeedsSudo() implements SkipDeleteReason {
            public String reason() {
                return "Needs higher permissions to delete the file";
            }
        }

        record JustCouldNot(String details) implements SkipDeleteReason {
            public String reason() {
                return "Could not delete the file: " + details;
            }
        }
    }

    @Override
    public NukeResult execute() {
        if (!request.dryRun && !request.yes) {
            request.assertShouldNuke.run();
        }

        val cliBinaryPath = Resolve.cliBinaryPath(ctx);
        val cliName = Resolve.cliName(cliBinaryPath);

        val astraHomes = Resolve.astraHomes(ctx);
        val astraRcs = Resolve.astraRcs(ctx);
        val shellRcFiles = Resolve.shellRcFilesWithAutocomplete(ctx, cliName);

        val shouldPreserveAstraRcs = request.preserveAstrarc.orElseGet(() -> {
            if (astraRcs.isEmpty()) {
                return false;
            }
            return !request.promptShouldDeleteAstrarc.apply(astraRcs, request.dryRun);
        });

        val res = mkResult(cliBinaryPath, shellRcFiles);

        deleteAstraRcs(astraRcs, shouldPreserveAstraRcs, res);
        deleteHomesAndBinary(cliBinaryPath, astraHomes, res);

        return res;
    }

    private @NotNull NukeResult mkResult(Path cliBinaryPath, Set<Path> shellRcFiles) {
        return new NukeResult(new LinkedHashSet<>(), shellRcFiles, new LinkedHashMap<>(), null, cliBinaryPath);
    }

    private void deleteAstraRcs(List<Path> astraRcs, Boolean shouldPreserveAstraRcs, NukeResult res) {
        astraRcs.forEach((path) -> {
            if (shouldPreserveAstraRcs) {
                res.skipped().put(path, new SkipDeleteReason.UserChoseToKeep());
            } else {
                delete(path, res);
            }
        });
    }

    private void deleteHomesAndBinary(Path cliBinaryPath, List<Path> homePaths, NukeResult res) {
        val binDesRes = (ctx.isWindows())
            ? deleteHomesAndBinaryWindows(homePaths, cliBinaryPath, res)
            : deleteHomesAndBinaryUnix(homePaths, cliBinaryPath, res);

        res.binaryDeleteResult(binDesRes);
    }

    public BinaryDeleteResult deleteHomesAndBinaryUnix(List<Path> homePaths, Path cliBinaryPath, NukeResult res) {
        for (val path : homePaths) {
            delete(path, res);
        }

        if (homePaths.stream().noneMatch(cliBinaryPath::startsWith)) {
            delete(cliBinaryPath, res);
        }

        return (needsSudo(cliBinaryPath))
            ? new BinaryNotWritable(cliBinaryPath)
            : new BinaryDeleted();
    }

    public BinaryDeleteResult deleteHomesAndBinaryWindows(List<Path> homePaths, Path cliBinaryPath, NukeResult res) {
        val homeFolderWhichAstraIsRunningInside = homePaths.stream()
            .filter(cliBinaryPath::startsWith)
            .findFirst();

        for (val folder : homePaths) {
            try {
                if (homeFolderWhichAstraIsRunningInside.equals(Optional.of(folder))) {
                    @Cleanup val astraHomeFiles = Files.list(folder);
                    astraHomeFiles.filter(f -> f.endsWith("/cli")).forEach((f) -> delete(f, res));
                } else {
                    delete(folder, res);
                }
            } catch (IOException e) {
                res.skipped().put(folder, new SkipDeleteReason.JustCouldNot(e.getMessage()));
            }
        }

        if (homeFolderWhichAstraIsRunningInside.isEmpty()) {
            delete(cliBinaryPath, res);
        }

        return new BinaryMustBeDeleted(
            (homeFolderWhichAstraIsRunningInside)
                .map((folder) -> "rmdir /s /q " + folder)
                .orElse("del " + cliBinaryPath)
        );
    }

    private void delete(Path path, NukeResult res) {
        if (needsSudo(path)) {
            res.skipped().put(path, new SkipDeleteReason.NeedsSudo());
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
            res.skipped().put(path, new SkipDeleteReason.JustCouldNot(e.getMessage()));
        }
    }

    private boolean needsSudo(Path path) {
        try {
            return !Files.isWritable(path);
        } catch (Exception e) {
            ctx.log().warn("Could not determine if file '", path.toString(), "' is writable: ", e.getMessage());
            return false;
        }
    }

    private static class Resolve {
        private static Path cliBinaryPath(CliContext ctx) {
            return ctx.log().loading("Resolving the binary's own path", (_) -> {
                val file = ProcessHandle.current()
                    .info()
                    .command()
                    .map(ctx::path);

                if (file.isEmpty() || !ImageInfo.inImageCode()) {
                    throw new AstraCliException(UNSUPPORTED_EXECUTION, """
                      @|bold,red Error: can not nuke the CLI when not running as a native image.|@
                    """);
                }

                return file.get();
            });
        }

        private static String cliName(Path cliBinaryPath) {
            return PathUtils.getFileNameString(cliBinaryPath);
        }

        private static List<Path> astraHomes(CliContext ctx) {
            return ctx.log().loading("Resolving all possible astra home locations", (_) -> {
                return ctx.properties().homeFolderLocations(ctx.isWindows()).all().stream()
                    .map((loc) -> ctx.path(loc.path()))
                    .filter(Files::exists)
                    .toList();
            });
        }

        private static List<Path> astraRcs(CliContext ctx) {
            return ctx.log().loading("Resolving all possible astrarc locations", (_) -> {
                return ctx.properties().rcFileLocations(ctx.isWindows()).all().stream()
                    .map((loc) -> ctx.path(loc.path()))
                    .filter(Files::exists)
                    .toList();
            });
        }

        private static Set<Path> shellRcFilesWithAutocomplete(CliContext ctx, String cliName) {
            return ctx.log().loading("Resolving any shell files which may contain astra-related statements", (_) -> {
                if (ctx.isWindows()) {
                    return new HashSet<>();
                }

                val autocompletePatterns = List.of(
                    Pattern.compile(
                        "^.*(?:source|\\.).*" + Pattern.quote(cliName) + "\\s+(compgen|shellenv).*$",
                        Pattern.MULTILINE
                    ),
                    Pattern.compile(
                        "^.*export\\s+ ASTRA.*=.*$",
                        Pattern.MULTILINE
                    )
                );

                return Stream.of(".bashrc", ".zshrc", ".profile", ".bash_profile", ".zprofile")
                    .map(name -> ctx.path(System.getProperty("user.home"), name))
                    .filter(Files::exists)
                    .filter((f) -> {
                        try {
                            val content = Files.readString(f);
                            return autocompletePatterns.stream().anyMatch((p) -> p.matcher(content).find());
                        } catch (Exception e) {
                            ctx.log().warn("Could not read file '", f.toString(), "' to check for any astra-cli autocomplete entries: ", e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toSet());
            });
        }
    }
}
