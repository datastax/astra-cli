package com.dtsx.astra.cli.gateways.downloads;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.core.properties.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.file.PathUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

@RequiredArgsConstructor
public class DownloadsGatewayImpl implements DownloadsGateway {
    private final CliContext ctx;

    @Override
    public Either<String, List<Path>> downloadCloudSecureBundles(DbRef ref, Collection<Datacenter> datacenters) {
        val result = new ArrayList<Path>();

        for (val datacenter : datacenters) {
            try {
                val scbName = "scb_%s_%s.zip".formatted(datacenter.getId(), datacenter.getRegion());
                val scbPath = ctx.home().dirs.scb.use().resolve(scbName);

                if (Files.notExists(scbPath)) {
                    ctx.log().loading("Downloading secure connect bundle for database %s in region %s".formatted(ctx.highlight(ref), ctx.highlight(datacenter.getRegion())), (_) -> {
                        FileUtils.downloadFile(datacenter.getSecureBundleUrl(), scbPath.getParent(), scbName);
                        return null;
                    });
                }

                result.add(scbPath);
            } catch (Exception e) {
                ctx.log().exception("Failed to download secure connect bundle for database '%s' in region '%s'".formatted(ref, datacenter.getRegion()));
                ctx.log().exception(e);
                return Either.left("Failed to download secure connect bundle for database '%s' in region '%s': %s".formatted(ref, datacenter.getRegion(), e.getMessage()));
            }
        }

        return Either.pure(result);
    }

    @Override
    public Either<String, Path> downloadCqlsh(ExternalSoftware cqlsh) { // also cqlsh@6.8.0
        return installArchiveIfNotExists(ctx.home().dirs.cqlsh(cqlsh.version()).use(), cqlsh, "cqlsh", Set.of("cqlsh-astra@6.8.0"), ctx);
    }

    @Override
    public Either<String, Path> downloadDsbulk(ExternalSoftware dsbulk) {
        return installArchiveIfNotExists(ctx.home().dirs.dsbulk(dsbulk.version()).use(), dsbulk, "dsbulk", Set.of("dsbulk@1.11.0"), ctx);
    }

    @Override
    public Either<String, Path> downloadPulsarShell(ExternalSoftware pulsar) {
        return installArchiveIfNotExists(ctx.home().dirs.pulsar(pulsar.version()).use(), pulsar, "pulsar-shell", Set.of("lunastreaming-shell@3.1.411"), ctx);
    }

    @Override
    public Either<String, Path> downloadAstra(ExternalSoftware astra) {
        try {
            // must use ctx.path(...) instead of letting Files.createTempDirectory use the default tmpdir
            // so that jimfs can be used in tests
            val tmpDirPath = ctx.path(System.getProperty("java.io.tmpdir"));

            // I think this is only necessary for jimfs reasons, doubt this would ever be triggered in the real world
            Files.createDirectories(tmpDirPath);

            val tmpDir = Files.createTempDirectory(tmpDirPath, "astra-cli-upgrade-").resolve("astra");

            val exeName = (astra.url().endsWith(".zip"))
                ? "astra.exe"
                : "astra";

            return installArchiveIfNotExists(tmpDir, astra, exeName, Set.of(), ctx);
        } catch (Exception e) {
            return Either.left("Failed to create temporary directory in %s (%s): '%s'".formatted(System.getProperty("java.io.tmpdir"), e.getClass().getSimpleName(), e.getMessage()));
        }
    }

    @Override
    public Optional<Path> cqlshPath(ExternalSoftware cqlsh) {
        return getExePath(ctx.home().dirs.cqlsh(cqlsh.version()).useIfExists(), "cqlsh");
    }

    @Override
    public Optional<Path> dsbulkPath(ExternalSoftware dsbulk) {
        return getExePath(ctx.home().dirs.dsbulk(dsbulk.version()).useIfExists(), "dsbulk");
    }

    @Override
    public Optional<Path> pulsarShellPath(ExternalSoftware pulsar) {
        return getExePath(ctx.home().dirs.pulsar(pulsar.version()).useIfExists(), "pulsar-shell");
    }

    private Optional<Path> getExePath(Optional<Path> subfolder, String exe) {
        if (subfolder.isPresent()) {
            val exeFile = subfolder.get().resolve("bin").resolve(exe);

            if (Files.exists(exeFile) && Files.isExecutable(exeFile)) {
                return Optional.of(exeFile);
            }
        }
        return Optional.empty();
    }

    private Either<String, Path> installArchiveIfNotExists(Path installDir, ExternalSoftware ex, String exeName, Set<String> legacyInstallDirs, CliContext ctx) {
        val existing = getExePath(Optional.of(installDir), exeName);

        if (existing.isPresent()) {
            return Either.pure(existing.get());
        }

        return installArchive(installDir, ex, exeName, legacyInstallDirs, ctx);
    }

    // WARNING: THIS ASSUMES THE ARCHIVE FOLLOWS THE STANDARD <name>/<bin>/<exe> STRUCTURE
    private Either<String, Path> installArchive(Path installDir, ExternalSoftware ex, String exeName, Set<String> legacyInstallDirs, CliContext ctx) {
        cleanupExistingInstallation(installDir, legacyInstallDirs, ctx);

        return downloadArchive(installDir, ex, exeName, ctx).flatMap((archivePath) -> {
            return extractAndSetupArchive(installDir, archivePath, ex.version(), exeName, ctx);
        });
    }

    private void cleanupExistingInstallation(Path installDir, Set<String> legacyInstallDirs, CliContext ctx) {
        tryCleanupLegacyFiles(installDir, legacyInstallDirs, ctx);

        try {
            if (Files.exists(installDir)) {
                PathUtils.deleteDirectory(installDir);
            }
            Files.createDirectories(installDir);
        } catch (Exception e) {
            ctx.log().exception("Issue cleaning up existing installation", e);
        }
    }

    @SneakyThrows
    @SuppressWarnings("resource")
    private void tryCleanupLegacyFiles(Path installDir, Set<String> legacyInstallDirs, CliContext ctx) {
        val homeDir = ctx.path(ctx.home().root());

        for (val legacyInstallDir : legacyInstallDirs) {
            val legacyPath = homeDir.resolve(legacyInstallDir);

            try {
                if (Files.exists(legacyPath)) {
                    ctx.log().debug("Cleaning up legacy installation at " + legacyPath);
                    PathUtils.deleteDirectory(legacyPath);
                }
            } catch (Exception e) {
                ctx.log().exception("Issue cleaning up legacy installation at " + legacyPath, e);
            }
        }

        val installDirParent = installDir.getParent();

        if (installDirParent != null) {
            for (val child : Files.walk(installDirParent, 1).skip(1).toList()) {
                if (!Files.isDirectory(child) || Version.parse(child.getFileName().toString()).isLeft()) {
                    try {
                        ctx.log().debug("Cleaning up legacy installation at " + child);
                        PathUtils.deleteDirectory(child);
                    } catch (Exception e) {
                        ctx.log().exception("Issue cleaning up legacy installation at " + child, e);
                    }
                }
            }
        }
    }

    private static @NotNull Either<String, Path> downloadArchive(Path installDir, ExternalSoftware ex, String exeName, CliContext ctx) {
        try {
            return ctx.log().loading("Downloading @!" + exeName + " v" + ex.version() + "!@, please wait", (_) -> {
                return Either.pure(
                    FileUtils.downloadFile(ex.url(), installDir, null)
                );
            });
        } catch (Exception e) {
            ctx.log().exception(e);
            return Either.left("Failed to download " + exeName + " archive from %s (%s): '%s'".formatted(ex.url(), e.getClass().getSimpleName(), e.getMessage()));
        }
    }

    private Either<String, Path> extractAndSetupArchive(Path installDir, Path archivePath, String version, String exeName, CliContext ctx) {
        try {
            ctx.log().loading("Extracting @!" + exeName + " v" + version + "!@, please wait", (_) -> {
                if (archivePath.toString().endsWith(".zip")) {
                    FileUtils.extractZipArchiveInPlace(archivePath, ctx);
                } else {
                    FileUtils.extractTarGzArchiveInPlace(archivePath, ctx);
                }
                return null;
            });
        } catch (Exception e) {
            ctx.log().exception(e);
            return Either.left("Failed to extract " + exeName + " archive %s (%s): '%s'".formatted(archivePath, e.getClass().getSimpleName(), e.getMessage()));
        }

        return findExtractedDirAndCleanLeftovers(installDir).flatMap((extractedDir) -> {
            return setupArchive(installDir, extractedDir, version, exeName, ctx);
        });
    }

    @SneakyThrows
    @SuppressWarnings("resource")
    private Either<String, Path> findExtractedDirAndCleanLeftovers(Path installDir) {
        val children = Files.list(installDir).toList();

        for (val child : children) {
            try {
                if (!Files.isDirectory(child)) {
                    Files.delete(child);
                }
            } catch (Exception e) {
                ctx.log().exception("Error checking/deleting leftover file from installation", e); // technically no harm if not cleaned, at least w/ existing installable programs
            }
        }

        val extractedDir = Files.list(installDir)
            .filter(Files::isDirectory)
            .toList();

        if (extractedDir.size() == 1) {
            return Either.pure(extractedDir.getFirst());
        } else {
            return Either.left("Failed to locate extracted directory in " + installDir + "; found " + extractedDir.size() + " directories (expected 1)");
        }
    }

    @SuppressWarnings("resource")
    private Either<String, Path> setupArchive(Path installDir, Path extractedDir, String version, String exe, CliContext ctx) {
        try {
            for (val item : Files.list(extractedDir).toList()) {
                Files.move(item, installDir.resolve(item.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
            PathUtils.deleteDirectory(extractedDir);
        } catch (Exception e) {
            ctx.log().exception(e);
            return Either.left("Failed to move extracted files (%s): '%s'".formatted(e.getClass().getSimpleName(), e.getMessage()));
        }

        val exeFile = installDir.resolve("bin/" + exe);

        try {
            Files.setPosixFilePermissions(exeFile, Set.of(
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ,
                PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE
            ));
        } catch (UnsupportedOperationException _) {
            // at least we can die happy, knowing that we tried...
        } catch (Exception e) {
            ctx.log().exception(e);
            return Either.left("Failed to set execute permissions on %s (%s): '%s'".formatted(exeFile, e.getClass().getSimpleName(), e.getMessage()));
        }

        return Either.pure(exeFile);
    }
}
