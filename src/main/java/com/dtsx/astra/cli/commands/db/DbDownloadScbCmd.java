package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbDownloadScbOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Optional;

import static com.dtsx.astra.cli.operations.db.DbDownloadScbOperation.*;

@Command(
    name = "download-scb",
    description = "Download secure connect bundle archive for a region"
)
public class DbDownloadScbCmd extends AbstractDbSpecificCmd<DownloadScbResult> {

    @Option(
        names = { "-r", "--region" },
        description = "Cloud provider region",
        paramLabel = "DB_REGION"
    )
    private Optional<RegionName> region;

    @Option(
        names = { "-f", "--output-file" },
        description = "Destination file",
        paramLabel = "DEST"
    )
    private Optional<File> destination;

    @Override
    protected DbDownloadScbOperation mkOperation() {
        return new DbDownloadScbOperation(dbGateway, new DbDownloadScbRequest(dbRef, region, destination));
    }

    @Override
    protected final OutputAll execute(DownloadScbResult result) {
        return switch (result) {
            case ScbDownloaded(var path) -> 
                OutputAll.message("Secure connect bundle downloaded to: " + path);
            case ScbDownloadedAndMoved(var originalPath, var destinationPath) -> 
                OutputAll.message("Secure connect bundle downloaded to: " + destinationPath);
        };
    }
}
