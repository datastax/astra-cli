package com.dtsx.astra.cli.gateways.downloads;

import com.dtsx.astra.cli.core.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
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

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class DownloadsGatewayImpl implements DownloadsGateway {
    private final APIProvider api;

    @Override
    public Either<String, List<Path>> downloadCloudSecureBundles(DbRef ref, String dbName, Collection<Datacenter> datacenters) {
        val dbOpsClient = api.dbOpsClient(ref);
        val result = new ArrayList<Path>();

        for (val datacenter : datacenters) {
            try {
                AstraLogger.loading("Downloading secure connect bundle for database %s in region %s".formatted(highlight(ref), highlight(datacenter.getRegion())), (_) -> {
                    val scbName = dbOpsClient.buildScbFileName(dbName, datacenter.getRegion());
                    val scbPath = AstraHome.Dirs.useScb().resolve(scbName);

                    if (Files.notExists(scbPath)) {
                        FileUtils.downloadFile(datacenter.getSecureBundleUrl(), scbPath);
                    }

                    result.add(scbPath);
                    return null;
                });
            } catch (Exception e) {
                AstraLogger.exception("Failed to download secure connect bundle for database '%s' in region '%s'".formatted(ref, datacenter.getRegion()));
                AstraLogger.exception(e);
                return Either.left("Failed to download secure connect bundle for database '%s' in region '%s': %s".formatted(ref, datacenter.getRegion(), e.getMessage()));
            }
        }

        return Either.right(result);
    }

    @Override
    public Either<String, Path> downloadCqlsh(ExternalSoftware cqlsh) {
        return installGenericArchive(AstraHome.Dirs.useCqlsh(), cqlsh.url(), "cqlsh");
    }

    @Override
    public Either<String, Path> downloadDsbulk(ExternalSoftware dsbulk) {
        return installGenericArchive(AstraHome.Dirs.useDsbulk(dsbulk.version()), dsbulk.url(), "dsbulk");
    }

    @Override
    public Either<String, Path> downloadPulsarShell(ExternalSoftware pulsar) {
        return installGenericArchive(AstraHome.Dirs.usePulsar(pulsar.version()), pulsar.url(), "pulsar-shell");
    }

    @Override
    public Optional<Path> cqlshPath(ExternalSoftware cqlsh) {
        return getPath(AstraHome.Dirs::cqlshExists, AstraHome.Dirs::useCqlsh, "cqlsh");
    }

    @Override
    public Optional<Path> dsbulkPath(ExternalSoftware dsbulk) {
        return getPath(() -> AstraHome.Dirs.dsbulkExists(dsbulk.version()), () -> AstraHome.Dirs.useDsbulk(dsbulk.version()), "dsbulk");
    }

    @Override
    public Optional<Path> pulsarShellPath(ExternalSoftware pulsar) {
        return getPath(() -> AstraHome.Dirs.pulsarExists(pulsar.version()), () -> AstraHome.Dirs.usePulsar(pulsar.version()), "pulsar-shell");
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
    private Either<String, Path> installGenericArchive(Path installDir, String url, String exe) {
        if (Files.isRegularFile(installDir)) {
            return Either.left("%s is a file; expected it to be a directory".formatted(installDir));
        }

        val tarFile = installDir.getParent().resolve(exe + "-download.tar.gz");
        val exeFile = installDir.resolve("bin/" + exe);

        if (!PathUtils.isEmptyDirectory(tarFile)) {
            return Either.right(exeFile);
        }

        Files.deleteIfExists(tarFile);

        try {
            AstraLogger.loading("Downloading " + exe + ", please wait", (_) -> {
                FileUtils.downloadFile(url, tarFile);
                return null;
            });
        } catch (Exception e) {
            return Either.left("Failed to download " + exe + " archive from %s: '%s'".formatted(url, e.getMessage()));
        }

        try {
            AstraLogger.loading("Extracting " + exe + " archive, please wait", (_) -> {
                FileUtils.extractTarArchiveInPlace(tarFile);
                return null;
            });
        } catch (Exception e) {
            AstraLogger.exception(e);
            return Either.left("Failed to extract " + exe + " archive %s: '%s'".formatted(tarFile, e.getMessage()));
        }

        try {
            Files.setPosixFilePermissions(exeFile, Set.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE));
        } catch (Exception e) {
            return Either.left("Failed to set execute permissions on %s: '%s'".formatted(exeFile, e.getMessage()));
        }

        try {
            Files.delete(tarFile);
        } catch (Exception e) {
            return Either.left("Failed to delete temporary " + exe + " archive %s: '%s'".formatted(tarFile, e.getMessage()));
        }

        return Either.right(exeFile);
    }
}
