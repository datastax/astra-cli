package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.properties.CliProperties.SupportedPackageManager;
import com.dtsx.astra.cli.operations.NukeOperation.NukeResult;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.io.file.PathUtils;
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
        Runnable assertShouldNuke,
        Runnable assertShouldNotUseWindowsUninstaller
    ) {}

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

    public sealed interface BinaryDeleteResult {}
    public record BinaryOwnedByPackageManager(SupportedPackageManager packageManager) implements BinaryDeleteResult {}
    public record BinaryMustBeDeleted(String deleteCommand) implements BinaryDeleteResult {}
    public record BinaryNotWritable(Path path) implements BinaryDeleteResult {}
    public record BinaryDeleted() implements BinaryDeleteResult {}

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

        if (!request.yes && Resolve.windowsUninstallerExists(ctx)) {
            request.assertShouldNotUseWindowsUninstaller.run();
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
        if (ctx.isWindows()) {
            deleteHomesWindows(homePaths, cliBinaryPath, res);
        } else {
            deleteHomesUnix(homePaths, res);
        }

        val pm = ctx.properties().owningPackageManager();

        res.binaryDeleteResult(
            (pm.isPresent())
                ? new BinaryOwnedByPackageManager(pm.get()) :
            (ctx.isWindows())
                ? deleteBinaryWindows(homePaths, cliBinaryPath)
                : deleteBinaryUnix(cliBinaryPath, res)
        );
    }

    public void deleteHomesUnix(List<Path> homePaths, NukeResult res) {
        for (val path : homePaths) {
            delete(path, res);
        }
    }

    public BinaryDeleteResult deleteBinaryUnix(Path cliBinaryPath, NukeResult res) {
        if (!Files.exists(cliBinaryPath)) {
            return new BinaryDeleted();
        }

        if (needsSudo(cliBinaryPath)) {
            return new BinaryNotWritable(cliBinaryPath);
        }

        delete(cliBinaryPath, res);

        return (Files.exists(cliBinaryPath) && !request.dryRun)
            ? new BinaryMustBeDeleted("rm " + cliBinaryPath)
            : new BinaryDeleted();
    }

    public void deleteHomesWindows(List<Path> homePaths, Path cliBinaryPath, NukeResult res) {
        for (val folder : homePaths) {
            try {
                if (cliBinaryPath.startsWith(folder)) {
                    @Cleanup val astraHomeFiles = Files.list(folder);
                    astraHomeFiles.filter(f -> f.endsWith("cli")).forEach((f) -> delete(f, res));
                } else {
                    delete(folder, res);
                }
            } catch (IOException e) {
                res.skipped().put(folder, new SkipDeleteReason.JustCouldNot(e.getMessage()));
            }
        }

        if (Resolve.windowsUninstallerExists(ctx)) {
            deleteWindowsRegistryKey(res);
        }
    }

    public BinaryDeleteResult deleteBinaryWindows(List<Path> homePaths, Path cliBinaryPath) {
        val homeFolderWhichAstraIsRunningInside = homePaths.stream()
            .filter(cliBinaryPath::startsWith)
            .findFirst();

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

    private void deleteWindowsRegistryKey(NukeResult res) {
        val registryKeyPath = Path.of("HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\AstraCLI");

        try {
            if (!request.dryRun) {
                val process = new ProcessBuilder(
                    "reg", "delete",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\AstraCLI",
                    "/f"
                ).start();

                val exitCode = process.waitFor();

                if (exitCode != 0) {
                    ctx.log().warn("Could not delete Windows registry key (exit code ", String.valueOf(exitCode), ")");
                    res.skipped().put(registryKeyPath, new SkipDeleteReason.JustCouldNot("Registry key deletion failed with exit code " + exitCode));
                    return;
                }
            }

            res.deletedFiles().add(registryKeyPath);
        } catch (Exception e) {
            ctx.log().warn("Could not delete Windows registry key: ", e.getMessage());
            res.skipped().put(registryKeyPath, new SkipDeleteReason.JustCouldNot(e.getMessage()));
        }
    }

    private static class Resolve {
        private static Path cliBinaryPath(CliContext ctx) {
            return ctx.log().loading("Resolving the binary's own path", (_) -> {
                val binaryPath = ctx.properties().binaryPath();

                if (binaryPath.isEmpty()) {
                    throw new AstraCliException(UNSUPPORTED_EXECUTION, """
                      @|bold,red Error: can not nuke the CLI when not running as a native image.|@
                    """);
                }

                return binaryPath.get();
            });
        }

        private static boolean windowsUninstallerExists(CliContext ctx) {
            if (!ctx.isWindows()) {
                return false;
            }

            try {
                val process = new ProcessBuilder(
                    "reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\AstraCLI"
                ).start();

                val exitCode = process.waitFor();
                return exitCode == 0;
            } catch (Exception e) {
                ctx.log().warn("Could not check for Windows uninstaller registry key: ", e.getMessage());
                return false;
            }
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
