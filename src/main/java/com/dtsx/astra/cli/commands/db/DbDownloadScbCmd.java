package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbDownloadScbOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.ExitCode.DOWNLOAD_ISSUE;
import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.DbDownloadScbOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "download-scb",
    description = "Download secure connect bundle for a database"
)
@Example(
    comment = "Download the SCB for a database, targeting the default region",
    command = "astra db download-scb my_db"
)
@Example(
    comment = "Download the SCB for a database, targeting a specific region",
    command = "astra db download-scb my_db -r us-east1"
)
@Example(
    comment = "Download the SCB for a database, saving it to a specific file",
    command = "astra db download-scb my_db -f /path/to/scb.zip"
)
public class DbDownloadScbCmd extends AbstractPromptForDbCmd<DownloadScbResult> {
    @Option(
        names = { "-r", "--region" },
        description = "The cloud provider region to target. Defaults to the default region of the database.",
        paramLabel = "DB_REGION"
    )
    private Optional<RegionName> $region;

    @Option(
        names = { "-f", "--output-file" },
        description = "Destination file. Defaults to `~/.astra/scb/scb_<name/id>_<region>.zip`",
        paramLabel = "DEST"
    )
    private Optional<File> $destination;

    @Override
    protected DbDownloadScbOperation mkOperation() {
        return new DbDownloadScbOperation(dbGateway, downloadsGateway, new DbDownloadScbRequest($dbRef, $region, $destination));
    }

    @Override
    protected final OutputAll execute(DownloadScbResult result) {
        return switch (result) {
            case ScbDownloaded(var path) -> handleScbDownloaded(path);
            case ScbDownloadedAndMoved(var path) -> handleScbDownloaded(path);
            case ScbDownloadedAndMoveFailed fail -> throwScbDownloadedAndMoveFailed(fail);
            case ScbDownloadFailed (var error) -> throwScbDownloadFailed(error);
        };
    }

    private OutputAll handleScbDownloaded(File file) {
        return OutputAll.response("""
          The secure connect bundle was download to the following path:
          %s
        """.formatted(
            highlight(file.getAbsolutePath())
        ), mkData(file));
    }

    private <T> T throwScbDownloadedAndMoveFailed(ScbDownloadedAndMoveFailed fail) {
        val errorMessage =
            (fail.ex() instanceof FileAlreadyExistsException)
                ? "The destination file already exists; please delete it or specify a different destination." :
            (fail.ex() instanceof NoSuchFileException)
                ? "The destination directory does not exist; please create it or specify a different destination."
                : "(%s) %s".formatted(
                    fail.ex().getClass().getSimpleName(),
                    fail.ex().getMessage()
                );

        val potentialFix =
            (fail.ex() instanceof FileAlreadyExistsException)
                ? NL + NL + renderComment("Potential fix:") + NL + renderCommand("rm " + fail.dest().getPath()) :
            (fail.ex() instanceof NoSuchFileException)
                ? NL + NL + renderComment("Potential fix:") + NL + renderCommand("mkdir -p " + fail.dest().getParentFile().getPath())
                : "";

        throw new AstraCliException(FILE_ISSUE, trimIndent("""
          @|bold,red Error: Failed to move secure connect bundle to specified destination.|@
        
          Downloaded to: %s
          Failed to move to: %s
        
          Cause: %s
        
          %s
        """).formatted(
            highlight(fail.source().getAbsolutePath()),
            highlight(fail.dest().getAbsolutePath()),
            errorMessage + potentialFix,
            fail.deleteSucceeded()
                ? "@|green The downloaded file has been successfully deleted; it no longer exists in the 'Downloaded to' location.|@"
                : "@|bold,yellow Warning: Failed to delete the downloaded file; it still exists in the 'Downloaded to' location.|@"
        ));
    }

    private <T> T throwScbDownloadFailed(String error) {
        throw new AstraCliException(DOWNLOAD_ISSUE, trimIndent("""
          @|bold,red Error: Failed to download secure connect bundle.|@
        
          Cause:
          %s
        """).formatted(error));
    }

    private Map<String, Object> mkData(File dest) {
        return Map.of(
            "file", dest.getAbsolutePath()
        );
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to download the secure connect bundle(s) for";
    }
}
