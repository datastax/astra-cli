package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.internal.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.operations.db.DbDownloadScbOperation.DownloadScbResult;

@RequiredArgsConstructor
public class DbDownloadScbOperation implements Operation<DownloadScbResult> {
    private final DbGateway dbGateway;
    private final DownloadsGateway downloadsGateway;
    private final DbDownloadScbRequest request;

    public sealed interface DownloadScbResult {}
    public record ScbDownloaded(File file) implements DownloadScbResult {}
    public record ScbDownloadFailed(String error) implements DownloadScbResult {}
    public record ScbDownloadedAndMoved(File dest) implements DownloadScbResult {}
    public record ScbDownloadedAndMoveFailed(File source, File dest, boolean deleteSucceeded, IOException ex) implements DownloadScbResult {}

    public record DbDownloadScbRequest(
        DbRef dbRef,
        Optional<RegionName> region,
        Optional<File> destination
    ) {}

    @Override
    public DownloadScbResult execute() {
        val db = dbGateway.findOne(request.dbRef);

        return downloadSCB(db).fold(
            ScbDownloadFailed::new,
            this::handleDownloadedFiles
        );
    }

    private DownloadScbResult handleDownloadedFiles(File downloadedFile) {
        if (request.destination.isEmpty()) {
            return new ScbDownloaded(downloadedFile);
        }

        val destFile = request.destination.get();

        try {
            Files.move(downloadedFile.toPath(), destFile.toPath());
            return new ScbDownloadedAndMoved(destFile);
        } catch (IOException e) {
            val deleteSucceeded = downloadedFile.delete();
            return new ScbDownloadedAndMoveFailed(downloadedFile, destFile, deleteSucceeded, e);
        }
    }

    private Either<String, File> downloadSCB(Database db) {
        return downloadsGateway.downloadCloudSecureBundles(
            request.dbRef,
            db.getInfo().getName(),
            List.of(resolveDatacenter(db))
        ).map(List::getFirst);
    }

    private Datacenter resolveDatacenter(Database db) {
        return request.region
            .map((r) -> (
                db.getInfo().getDatacenters().stream()
                    .filter(dc -> dc.getRegion().equalsIgnoreCase(r.unwrap()))
                    .findFirst()
                    .orElseThrow(() -> new RegionNotFoundException(request.dbRef, r))
            ))
            .orElseGet(() -> (
                db.getInfo().getDatacenters().stream().findFirst().orElseThrow()
            ));
    }
}
