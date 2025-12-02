package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.properties.CliProperties.AstraBinary;
import com.dtsx.astra.cli.core.properties.CliProperties.AstraJar;
import com.dtsx.astra.cli.core.properties.CliProperties.PathToAstra;
import com.dtsx.astra.cli.core.properties.CliProperties.SupportedPackageManager;
import com.dtsx.astra.cli.operations.NukeOperation.NukeResult;
import com.dtsx.astra.cli.operations.NukeOperation.SkipDeleteReason.JustCouldNot;
import com.dtsx.astra.cli.operations.NukeOperation.SkipDeleteReason.NeedsSudo;
import com.dtsx.astra.cli.operations.NukeOperation.SkipDeleteReason.UserChoseToKeep;
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
        private PathToAstra cliPath;
    }

    public sealed interface BinaryDeleteResult {}
    public record BinaryOwnedByPackageManager(SupportedPackageManager packageManager) implements BinaryDeleteResult {}
    public record CLIMustBeDeleted(String deleteCommand) implements BinaryDeleteResult {}
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

        val cliPath = Resolve.cliPath(ctx);
        val cliName = Resolve.cliName(cliPath);

        val astraHomes = Resolve.astraHomes(ctx);
        val astraRcs = Resolve.astraRcs(ctx);
        val shellRcFiles = Resolve.shellRcFilesWithAutocomplete(ctx, cliName);

        val shouldPreserveAstraRcs = request.preserveAstrarc.orElseGet(() -> {
            if (astraRcs.isEmpty()) {
                return false;
            }
            return !request.promptShouldDeleteAstrarc.apply(astraRcs, request.dryRun);
        });

        val res = mkResult(cliPath, shellRcFiles);

        deleteAstraRcs(astraRcs, shouldPreserveAstraRcs, res);
        deleteHomesAndBinary(cliPath, astraHomes, res);

        return res;
    }

    private @NotNull NukeResult mkResult(PathToAstra cliPath, Set<Path> shellRcFiles) {
        return new NukeResult(new LinkedHashSet<>(), shellRcFiles, new LinkedHashMap<>(), null, cliPath);
    }

    private void deleteAstraRcs(List<Path> astraRcs, Boolean shouldPreserveAstraRcs, NukeResult res) {
        astraRcs.forEach((path) -> {
            if (shouldPreserveAstraRcs) {
                res.skipped().put(path, new UserChoseToKeep());
            } else {
                delete(path, res);
            }
        });
    }

    private void deleteHomesAndBinary(PathToAstra cliPath, List<Path> homePaths, NukeResult res) {
        if (ctx.isWindows()) {
            deleteHomesWindows(homePaths, cliPath, res);
        } else {
            deleteHomesUnix(homePaths, res);
        }

        var binDelRes = new Object() {
            BinaryDeleteResult ref;
        };

        ctx.properties().owningPackageManager().ifPresent((pm) -> {
            binDelRes.ref = new BinaryOwnedByPackageManager(pm);
        });

        if (binDelRes.ref == null) {
            binDelRes.ref = switch (cliPath) {
                case AstraJar(var jarPath) when ctx.isWindows() -> new CLIMustBeDeleted("del " + jarPath);
                case AstraJar(var jarPath) -> new CLIMustBeDeleted("rm " + jarPath);
                case AstraBinary bin -> (ctx.isWindows())
                    ? deleteBinaryWindows(homePaths, bin)
                    : deleteBinaryUnix(bin, res);
            };
        }

        res.binaryDeleteResult(binDelRes.ref);
    }

    public void deleteHomesUnix(List<Path> homePaths, NukeResult res) {
        for (val path : homePaths) {
            delete(path, res);
        }
    }

    public BinaryDeleteResult deleteBinaryUnix(AstraBinary cliPath, NukeResult res) {
        if (!Files.exists(cliPath.unwrap())) {
            return new BinaryDeleted();
        }

        if (needsSudo(cliPath.unwrap())) {
            return new BinaryNotWritable(cliPath.unwrap());
        }

        delete(cliPath.unwrap(), res);

        return (Files.exists(cliPath.unwrap()) && !request.dryRun)
            ? new CLIMustBeDeleted("rm " + cliPath.unwrap())
            : new BinaryDeleted();
    }

    public void deleteHomesWindows(List<Path> homePaths, PathToAstra cliPath, NukeResult res) {
        for (val folder : homePaths) {
            try {
                if (cliPath.unwrap().startsWith(folder)) {
                    @Cleanup val astraHomeFiles = Files.list(folder);
                    astraHomeFiles.filter(f -> f.endsWith("cli")).forEach((f) -> delete(f, res));
                } else {
                    delete(folder, res);
                }
            } catch (IOException e) {
                res.skipped().put(folder, new JustCouldNot(e.getMessage()));
            }
        }

        if (Resolve.windowsUninstallerExists(ctx)) {
            deleteWindowsRegistryKey(res);
        }
    }

    public BinaryDeleteResult deleteBinaryWindows(List<Path> homePaths, AstraBinary cliPath) {
        val homeFolderWhichAstraIsRunningInside = homePaths.stream()
            .filter((folder) -> cliPath.unwrap().startsWith(folder))
            .findFirst();

        return new CLIMustBeDeleted(
            (homeFolderWhichAstraIsRunningInside)
                .map((folder) -> "rmdir /s /q " + folder)
                .orElse("del " + cliPath.unwrap())
        );
    }

    private void delete(Path path, NukeResult res) {
        if (needsSudo(path)) {
            res.skipped().put(path, new NeedsSudo());
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
            res.skipped().put(path, new JustCouldNot(e.getMessage()));
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
                    res.skipped().put(registryKeyPath, new JustCouldNot("Registry key deletion failed with exit code " + exitCode));
                    return;
                }
            }

            res.deletedFiles().add(registryKeyPath);
        } catch (Exception e) {
            ctx.log().warn("Could not delete Windows registry key: ", e.getMessage());
            res.skipped().put(registryKeyPath, new JustCouldNot(e.getMessage()));
        }
    }

    private static class Resolve {
        private static PathToAstra cliPath(CliContext ctx) {
            return ctx.log().loading("Resolving the binary's own path", (_) -> {
                return ctx.properties().cliPath(ctx);
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

        private static String cliName(PathToAstra cliPath) {
            return switch (cliPath) {
                case AstraBinary(var binaryPath) -> binaryPath.getFileName().toString();
                case AstraJar _ -> "astra";
            };
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
