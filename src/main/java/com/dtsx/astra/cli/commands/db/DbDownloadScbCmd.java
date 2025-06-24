package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbDownloadScbOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Optional;

@Command(
    name = "download-scb",
    description = "Download secure connect bundle archive for a region"
)
public final class DbDownloadScbCmd extends AbstractDbSpecificCmd {

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
    public OutputAll execute() {
        val operation = new DbDownloadScbOperation(dbGateway);
        val result = operation.execute(dbRef, region, destination);
        
        return switch (result) {
            case DbDownloadScbOperation.ScbDownloaded(var path) -> 
                OutputAll.message("Secure connect bundle downloaded to: " + path);
            case DbDownloadScbOperation.ScbDownloadedAndMoved(var originalPath, var destinationPath) -> 
                OutputAll.message("Secure connect bundle downloaded to: " + destinationPath);
        };
    }
}
