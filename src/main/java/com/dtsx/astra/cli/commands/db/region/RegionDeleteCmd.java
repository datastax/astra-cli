package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.region.RegionDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.REGION_NOT_FOUND;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.region.RegionDeleteOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "delete-region",
    description = "Delete a region from an existing database"
)
@Example(
    comment = "Delete a region from a database",
    command = "astra db delete-region my_db -r us-west-2"
)
@Example(
    comment = "Delete a region without failing if it doesn't exist",
    command = "astra db delete-region my_db -r us-west-2 --if-exists"
)
@Example(
    comment = "Delete a region without waiting for the database to become active",
    command = "astra db delete-region my_db -r us-west-2 --async"
)
public class RegionDeleteCmd extends AbstractLongRunningRegionRequiredCmd<RegionDeleteResult> {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if region does not exist", DEFAULT_VALUE }
    )
    public boolean $ifExists;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected RegionDeleteOperation mkOperation() {
        return new RegionDeleteOperation(regionGateway, dbGateway, new RegionDeleteRequest($dbRef, $region, $ifExists, lrMixin.options()));
    }

    @Override
    protected final OutputAll execute(RegionDeleteResult result) {
        val message = switch (result) {
            case RegionNotFound() -> handleRegionNotFound();
            case RegionDeleted() -> handleRegionDeleted();
            case RegionDeletedAndDbActive(var waitTime) -> handleRegionDeletedAndDbActive(waitTime);
            case RegionIllegallyNotFound() -> throwRegionNotFound();
        };
        
        return OutputAll.message(trimIndent(message));
    }

    private String handleRegionNotFound() {
        return """
          Region %s does not exist in database %s; nothing to delete.
          
          %s
          %s
          
          %s
          %s
        """.formatted(
            highlight($region),
            highlight($dbRef),
            renderComment("List all regions in the database:"),
            renderCommand("astra db list-regions %s".formatted($dbRef)),
            renderComment("Get database information:"),
            renderCommand("astra db get %s".formatted($dbRef))
        );
    }

    private String handleRegionDeleted() {
        return """
          Region %s has been deleted from database %s (database may not be active yet).
          
          %s
          %s
          
          %s
          %s
        """.formatted(
            highlight($region),
            highlight($dbRef),
            renderComment("Poll the database's status:"),
            renderCommand("astra db status %s".formatted($dbRef)),
            renderComment("List remaining regions in the database:"),
            renderCommand("astra db list-regions %s".formatted($dbRef))
        );
    }

    private String handleRegionDeletedAndDbActive(Duration waitTime) {
        return """
          Region %s has been deleted from database %s.
          
          Database is now active after waiting %s seconds.
          
          %s
          %s
        """.formatted(
            highlight($region),
            highlight($dbRef),
            highlight(waitTime.toSeconds()),
            renderComment("List remaining regions in the database:"),
            renderCommand("astra db list-regions %s".formatted($dbRef))
        );
    }

    private String throwRegionNotFound() {
        throw new AstraCliException(REGION_NOT_FOUND, """
          @|bold,red Error: Region %s does not exist in database %s.|@
          
          To ignore this error, provide the %s flag to skip this error if the region doesn't exist.
          
          %s
          %s
          
          %s
          %s
        """.formatted(
            $region,
            $dbRef,
            highlight("--if-exists"),
            renderComment("Example fix:"),
            renderCommand(originalArgs(), "--if-exists"),
            renderComment("List existing regions:"),
            renderCommand("astra db list-regions %s".formatted($dbRef))
        ));
    }
}
