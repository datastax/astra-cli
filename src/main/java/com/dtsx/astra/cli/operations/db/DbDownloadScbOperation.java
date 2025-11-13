package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.utils.DbUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.operations.db.DbDownloadScbOperation.DownloadScbResult;

@RequiredArgsConstructor
public class DbDownloadScbOperation implements Operation<DownloadScbResult> {
    private final DbGateway dbGateway;
    private final DownloadsGateway downloadsGateway;
    private final DbDownloadScbRequest request;

    public sealed interface DownloadScbResult {}
    public record ScbDownloaded(Path file) implements DownloadScbResult {}
    public record ScbDownloadFailed(String error) implements DownloadScbResult {}
    public record ScbDownloadedAndMoved(Path dest) implements DownloadScbResult {}
    public record ScbDownloadedAndMoveFailed(Path source, Path dest, boolean deleteSucceeded, IOException ex) implements DownloadScbResult {}
    public record ScbDestinationAlreadyExists() implements DownloadScbResult {}
    public record ScbInvalidDestination(String reason) implements DownloadScbResult {}

    public record DbDownloadScbRequest(
        DbRef dbRef,
        Optional<RegionName> region,
        Optional<Path> destination
    ) {}

    @Override
    public DownloadScbResult execute() {
        val destinationError = request.destination.flatMap(this::validateDestinationFile);

        if (destinationError.isPresent()) {
            return destinationError.get();
        }

        val db = dbGateway.findOne(request.dbRef);

        return downloadSCB(db).fold(
            ScbDownloadFailed::new,
            this::handleDownloadedFiles
        );
    }

    private Optional<DownloadScbResult> validateDestinationFile(Path path) {
        if (!path.toString().endsWith(".zip")) {
            return Optional.of(new ScbInvalidDestination("Destination file " + path + " is not a .zip file."));
        }

        if (Files.exists(path)) {
            return Optional.of(new ScbDestinationAlreadyExists());
        }

        return Optional.empty();
    }

    private DownloadScbResult handleDownloadedFiles(Path downloadedFile) {
        if (request.destination.isEmpty()) {
            return new ScbDownloaded(downloadedFile);
        }

        val destFile = request.destination.get();

        try {
            Files.move(downloadedFile, destFile);
            return new ScbDownloadedAndMoved(destFile);
        } catch (IOException e) {
            try {
                Files.delete(downloadedFile);
                return new ScbDownloadedAndMoveFailed(downloadedFile, destFile, true, e);
            } catch (IOException ex) {
                return new ScbDownloadedAndMoveFailed(downloadedFile, destFile, false, e);
            }
        }
    }

    private Either<String, Path> downloadSCB(Database db) {
        return downloadsGateway.downloadCloudSecureBundles(
            request.dbRef,
            List.of(DbUtils.resolveDatacenter(db, request.region))
        ).map(List::getFirst);
    }
}
