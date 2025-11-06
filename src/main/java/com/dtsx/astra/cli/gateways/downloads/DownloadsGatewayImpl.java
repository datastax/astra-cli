package com.dtsx.astra.cli.gateways.downloads;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.properties.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.file.PathUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DownloadsGatewayImpl implements DownloadsGateway {
    private final CliContext ctx;

    @Override
    public Either<String, List<Path>> downloadCloudSecureBundles(DbRef ref, Collection<Datacenter> datacenters) {
        val result = new ArrayList<Path>();

        for (val datacenter : datacenters) {
            try {
                ctx.log().loading("Downloading secure connect bundle for database %s in region %s".formatted(ctx.highlight(ref), ctx.highlight(datacenter.getRegion())), (_) -> {
                    val scbName = "scb_%s_%s.zip".formatted(datacenter.getId(), datacenter.getRegion());
                    val scbPath = ctx.home().dirs().useScb().resolve(scbName);

                    if (Files.notExists(scbPath)) {
                        FileUtils.downloadFile(datacenter.getSecureBundleUrl(), scbPath);
                    }

                    result.add(scbPath);
                    return null;
                });
            } catch (Exception e) {
                ctx.log().exception("Failed to download secure connect bundle for database '%s' in region '%s'".formatted(ref, datacenter.getRegion()));
                ctx.log().exception(e);
                return Either.left("Failed to download secure connect bundle for database '%s' in region '%s': %s".formatted(ref, datacenter.getRegion(), e.getMessage()));
            }
        }

        return Either.pure(result);
    }

    @Override
    public Either<String, Path> downloadCqlsh(ExternalSoftware cqlsh) {
        return installGenericArchive(ctx.home().dirs().useCqlsh(cqlsh.version()), cqlsh.url(), cqlsh.version(), "cqlsh", ctx);
    }

    @Override
    public Either<String, Path> downloadDsbulk(ExternalSoftware dsbulk) {
        return installGenericArchive(ctx.home().dirs().useDsbulk(dsbulk.version()), dsbulk.url(), dsbulk.version(), "dsbulk", ctx);
    }

    @Override
    public Either<String, Path> downloadPulsarShell(ExternalSoftware pulsar) {
        return installGenericArchive(ctx.home().dirs().usePulsar(pulsar.version()), pulsar.url(), pulsar.version(), "pulsar-shell", ctx);
    }

    @Override
    public Either<String, Path> downloadAstra(ExternalSoftware astra) {
        try {
            val tmpDir = Files.createTempDirectory(ctx.path(System.getProperty("java.io.tmpdir")), "astra-cli-upgrade-").resolve("astra");
            return installGenericArchive(tmpDir, astra.url(), astra.version(), "astra", ctx);
        } catch (Exception e) {
            return Either.left("Failed to create temporary directory in %s: '%s'".formatted(System.getProperty("java.io.tmpdir"), e.getMessage()));
        }
    }

    @Override
    public Optional<Path> cqlshPath(ExternalSoftware cqlsh) {
        return getPath(() -> ctx.home().dirs().cqlshExists(cqlsh.version()), () -> ctx.home().dirs().useCqlsh(cqlsh.version()), "cqlsh");
    }

    @Override
    public Optional<Path> dsbulkPath(ExternalSoftware dsbulk) {
        return getPath(() -> ctx.home().dirs().dsbulkExists(dsbulk.version()), () -> ctx.home().dirs().useDsbulk(dsbulk.version()), "dsbulk");
    }

    @Override
    public Optional<Path> pulsarShellPath(ExternalSoftware pulsar) {
        return getPath(() -> ctx.home().dirs().pulsarExists(pulsar.version()), () -> ctx.home().dirs().usePulsar(pulsar.version()), "pulsar-shell");
    }

    private Optional<Path> getPath(Supplier<Boolean> dirExists, Supplier<Path> getDir, String exe) {
        if (dirExists.get()) {
            val exeFile = getDir.get().resolve("bin/" + exe);

            if (Files.exists(exeFile) && Files.isExecutable(exeFile)) {
                return Optional.of(exeFile);
            }
        }
        return Optional.empty();
    }

    @SneakyThrows
    private Either<String, Path> installGenericArchive(Path installDir, String url, Version version, String exe, CliContext ctx) {
        if (Files.isRegularFile(installDir)) {
            return Either.left("%s is a file; expected it to be a directory".formatted(installDir));
        }

        val suffix = (ctx.isWindows()) ? ".zip" : ".tar.gz";

        val archiveFile = installDir.resolve(exe + "-download" + suffix);
        val exeFile = installDir.resolve("bin/" + exe);

        if (PathUtils.isRegularFile(exeFile) && !PathUtils.isEmptyFile(exeFile)) {
            return Either.pure(exeFile);
        }

        try {
            FileUtils.createDirIfNotExists(installDir, null);
            PathUtils.cleanDirectory(installDir);
            Files.deleteIfExists(archiveFile);
        } catch (Exception e) {
            ctx.log().exception(e);
            return Either.left("Failed to clean existing " + exe + " installation in %s (%s): '%s'".formatted(installDir, e.getClass().getSimpleName(), e.getMessage()));
        }

        try {
            ctx.log().loading("Downloading @!" + exe + " v" + version + "!@, please wait", (_) -> {
                FileUtils.downloadFile(url, archiveFile);
                return null;
            });
        } catch (Exception e) {
            ctx.log().exception(e);
            return Either.left("Failed to download " + exe + " archive from %s (%s): '%s'".formatted(url, e.getClass().getSimpleName(), e.getMessage()));
        }

        try {
            ctx.log().loading("Extracting @!" + exe  + " v" + version + "!@, please wait", (_) -> {
                if (archiveFile.endsWith(".zip")) {
                    FileUtils.extractZipArchiveInPlace(archiveFile, ctx);
                } else {
                    FileUtils.extractTarGzArchiveInPlace(archiveFile, ctx);
                }
                return null;
            });
        } catch (Exception e) {
            ctx.log().exception(e);
            return Either.left("Failed to extract " + exe + " archive %s (%s): '%s'".formatted(archiveFile, e.getClass().getSimpleName(), e.getMessage()));
        }

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

        try {
            Files.delete(archiveFile);
        } catch (Exception e) {
            ctx.log().exception(e);
            return Either.left("Failed to delete temporary " + exe + " archive %s (%s): '%s'".formatted(archiveFile, e.getClass().getSimpleName(), e.getMessage()));
        }

        return Either.pure(exeFile);
    }
}
