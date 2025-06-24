package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.operations.db.DbDownloadScbOperation.*;

@RequiredArgsConstructor
public class DbDownloadScbOperation implements Operation<DownloadScbResult> {
    private final DbGateway dbGateway;
    private final DbDownloadScbRequest request;

    public sealed interface DownloadScbResult {}

    public record ScbDownloaded(String path) implements DownloadScbResult {}
    public record ScbDownloadedAndMoved(String originalPath, String destinationPath) implements DownloadScbResult {}

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
            .map(r -> db.getInfo().getDatacenters().stream()
                .filter(dc -> dc.getRegion().equalsIgnoreCase(r.unwrap()))
                .findFirst()
                .orElseThrow(() -> new RegionNotFoundException(request.dbRef, r))
            )
            .orElseGet(() -> {
                val datacenters = db.getInfo().getDatacenters();

                if (datacenters.size() > 1) {
                    throw new MultipleRegionsFoundException(request.dbRef, datacenters.stream().map(Datacenter::getRegion).toList());
                }

                return datacenters.stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No datacenters found for database"));
            });

        val paths = dbGateway.downloadCloudSecureBundles(request.dbRef, dbName, List.of(datacenter));
        
        if (paths.isEmpty()) {
            throw new RuntimeException("Failed to download secure connect bundle");
        }
        
        val downloadedPath = paths.getFirst();
        
        if (request.destination.isPresent()) {
            val destFile = request.destination.get();
            val sourceFile = new File(downloadedPath);
            
            if (sourceFile.renameTo(destFile)) {
                return new ScbDownloadedAndMoved(downloadedPath, destFile.getAbsolutePath());
            } else {
                val deleteSucceeded = sourceFile.delete();
                throw new ScbMoveFailedException(downloadedPath, destFile.getAbsolutePath(), deleteSucceeded);
            }
        }
        
        return new ScbDownloaded(downloadedPath);
    }

    public static class MultipleRegionsFoundException extends AstraCliException {
        public MultipleRegionsFoundException(DbRef dbRef, List<String> regions) {
            super("""
              @|bold,red Error: Multiple regions found for database '%s'.|@
            
              Available regions: %s
            
              Please specify a region using the %s option.
            """.formatted(
                dbRef,
                regions.stream().map(AstraColors::highlight).reduce("", (a, b) -> a + ", " + b),
                AstraColors.highlight("-r, --region")
            ));
        }
    }

    public static class ScbMoveFailedException extends AstraCliException {
        public ScbMoveFailedException(String downloadedPath, String destinationPath, boolean deleteSucceeded) {
            super("""
              @|bold,red Error: Failed to move secure connect bundle to specified destination.|@
            
              Downloaded to: %s
              Failed to move to: %s
            
              The file may be in use or the destination directory may not exist.
              %s
            """.formatted(
                AstraColors.highlight(downloadedPath),
                AstraColors.highlight(destinationPath),
                deleteSucceeded 
                    ? "Original file has been cleaned up."
                    : "@|bold,yellow Warning: Failed to clean up original file.|@"
            ));
        }
    }
}
