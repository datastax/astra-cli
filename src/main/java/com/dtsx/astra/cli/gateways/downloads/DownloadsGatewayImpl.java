package com.dtsx.astra.cli.gateways.downloads;

import com.dtsx.astra.cli.CLIProperties.ExternalSoftware;
import com.dtsx.astra.cli.config.AstraHome;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class DownloadsGatewayImpl implements DownloadsGateway {
    private final APIProvider api;

    @Override
    public Either<String, List<File>> downloadCloudSecureBundles(DbRef ref, String dbName, Collection<Datacenter> datacenters) {
        val dbOpsClient = api.dbOpsClient(ref);
        val result = new ArrayList<File>();

        for (val datacenter : datacenters) {
            try {
                AstraLogger.loading("Downloading secure connect bundle for database %s in region %s".formatted(highlight(ref), highlight(datacenter.getRegion())), (_) -> {
                    val scbName = dbOpsClient.buildScbFileName(dbName, datacenter.getRegion());
                    val scbPath = new File(AstraHome.Dirs.useScb(), scbName);

                    if (!scbPath.exists()) {
                        FileUtils.downloadFile(datacenter.getSecureBundleUrl(), scbPath.getAbsolutePath());
                    }

                    result.add(scbPath);
                    return null;
                });
            } catch (Exception e) {
                return Either.left("Failed to download secure connect bundle for database %s in region %s: %s".formatted(highlight(ref), highlight(datacenter.getRegion()), e.getMessage()));
            }
        }

        return Either.right(result);
    }

    @Override
    public Either<String, File> downloadCqlsh(ExternalSoftware cqlsh) {
        return installGenericArchive(AstraHome.Dirs.useCqlsh(), cqlsh.url(), "cqlsh");
    }

    @Override
    public Either<String, File> downloadDsbulk(ExternalSoftware dsbulk) {
        return installGenericArchive(AstraHome.Dirs.useDsbulk(dsbulk.version()), dsbulk.url(), "dsbulk");
    }

    @Override
    public Either<String, File> downloadPulsarShell(ExternalSoftware pulsar) {
        return installGenericArchive(AstraHome.Dirs.usePulsar(pulsar.version()), pulsar.url(), "pulsar-shell");
    }

    private Either<String, File> installGenericArchive(File installDir, String url, String exe) {
        if (installDir.isFile()) {
            return Either.left("%s is a file; expected it to be a directory".formatted(installDir.getAbsolutePath()));
        }

        val tarFile = new File(installDir, exe + "-download.tar.gz");
        val exeFile = new File(installDir, "bin/" + exe);

        if (Objects.requireNonNull(installDir.list()).length > 0) {
            return Either.right(exeFile);
        }

        //noinspection ResultOfMethodCallIgnored
        tarFile.delete();

        try {
            AstraLogger.loading("Downloading " + exe + ", please wait", (_) -> {
                FileUtils.downloadFile(url, tarFile.getAbsolutePath());
                return null;
            });
        } catch (Exception e) {
            return Either.left("Failed to download " + exe + " archive from %s: %s".formatted(url, e.getMessage()));
        }

        try {
            AstraLogger.loading("Extracting " + exe + " archive, please wait", (_) -> {
                FileUtils.extractTarArchiveInPlace(tarFile);
                return null;
            });
        } catch (Exception e) {
            return Either.left("Failed to extract " + exe + " archive %s: %s".formatted(tarFile.getAbsolutePath(), e.getMessage()));
        }

        if (!tarFile.delete()) {
            return Either.left("Failed to delete temporary " + exe + " archive %s".formatted(tarFile.getAbsolutePath()));
        }

        if (!exeFile.setExecutable(true, false)) {
            return Either.left("Failed to make " + exe + " executable at %s".formatted(exeFile.getAbsolutePath()));
        }

        return Either.right(exeFile);
    }
}
