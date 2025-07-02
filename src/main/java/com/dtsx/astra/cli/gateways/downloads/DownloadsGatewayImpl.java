package com.dtsx.astra.cli.gateways.downloads;

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
    public Either<String, File> downloadCqlshArchive(String url, String tarFileName) {
        val cqlshDir = AstraHome.Dirs.useCqlsh();

        if (cqlshDir.isFile()) {
            return Either.left("%s is a file; expected it to be a directory".formatted(cqlshDir.getAbsolutePath()));
        }

        val tarFile = new File(cqlshDir, tarFileName);
        val exeFile = new File(cqlshDir, "bin/cqlsh");

        if (Objects.requireNonNull(cqlshDir.list()).length > 0) {
            return Either.right(exeFile);
        }

        try {
            AstraLogger.loading("Downloading cqlsh, please wait", (_) -> {
                FileUtils.downloadFile(url, tarFile.getAbsolutePath());
                return null;
            });
        } catch (Exception e) {
            return Either.left("Failed to download cqlsh archive from %s: %s".formatted(url, e.getMessage()));
        }

        try {
            AstraLogger.loading("Extracting cqlsh archive, please wait", (_) -> {
                FileUtils.extractTarArchiveInPlace(tarFile);
                return null;
            });
        } catch (Exception e) {
            return Either.left("Failed to extract cqlsh archive %s: %s".formatted(tarFile.getAbsolutePath(), e.getMessage()));
        }

        if (!tarFile.delete()) {
            return Either.left("Failed to delete temporary cqlsh archive %s".formatted(tarFile.getAbsolutePath()));
        }

        if (!exeFile.setExecutable(true, false)) {
            return Either.left("Failed to make cqlsh executable at %s".formatted(exeFile.getAbsolutePath()));
        }

        return Either.right(exeFile);
    }
}
