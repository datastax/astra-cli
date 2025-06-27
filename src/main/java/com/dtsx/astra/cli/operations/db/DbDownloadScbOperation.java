package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.exceptions.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
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
    private final DbDownloadScbRequest request;

    public sealed interface DownloadScbResult {}

    public record ScbDownloaded(File file) implements DownloadScbResult {}
    public record ScbDownloadedAndMoved(File dest) implements DownloadScbResult {}
    public record ScbDownloadedAndMoveFailed(File source, File dest, boolean deleteSucceeded, IOException ex) implements DownloadScbResult {}

    public record DbDownloadScbRequest(
        DbRef dbRef,
        Optional<RegionName> region,
        Optional<File> destination
    ) {}

    @Override
    public DownloadScbResult execute() {
        val db = dbGateway.findOneDb(request.dbRef);
        val dbName = db.getInfo().getName();
        
        val datacenter = request.region
            .map((r) -> (
                db.getInfo().getDatacenters().stream()
                    .filter(dc -> dc.getRegion().equalsIgnoreCase(r.unwrap()))
                    .findFirst()
                    .orElseThrow(() -> new RegionNotFoundException(request.dbRef, r))
            ))
            .orElseGet(() -> (
                db.getInfo().getDatacenters().stream().findFirst().orElseThrow()
            ));

        val downloadedPath = dbGateway.downloadCloudSecureBundles(request.dbRef, dbName, List.of(datacenter)).getFirst();
        
        if (request.destination.isPresent()) {
            val destFile = request.destination.get();
            val sourceFile = new File(downloadedPath);

            try {
                Files.move(sourceFile.toPath(), destFile.toPath());
                return new ScbDownloadedAndMoved(destFile);
            } catch (IOException e) {
                val deleteSucceeded = sourceFile.delete();
                return new ScbDownloadedAndMoveFailed(sourceFile, destFile, deleteSucceeded, e);
            }
        }
        
        return new ScbDownloaded(new File(downloadedPath));
    }
}
