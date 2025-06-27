package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.region.RegionCreateOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.REGION_ALREADY_EXISTS;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.region.RegionCreateOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "create-region",
    description = "Add a new region to an existing database"
)
@Example(
    comment = "Create a region in a database",
    command = "astra db create-region my_db -r us-west-2"
)
@Example(
    comment = "Create a region without failing if it already exists",
    command = "astra db create-region my_db -r us-west-2 --if-not-exists"
)
@Example(
    comment = "Create a region without waiting for the database to become active",
    command = "astra db create-region my_db -r us-west-2 --async"
)
public class RegionCreateCmd extends AbstractLongRunningRegionRequiredCmd<RegionCreateResult> {
    @Option(
        names = { "--if-not-exists" },
        description = { "Do not fail if the region already exists", DEFAULT_VALUE }
    )
    public boolean $ifNotExists;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected RegionCreateOperation mkOperation() {
        return new RegionCreateOperation(regionGateway, dbGateway, new RegionCreateRequest($dbRef, $region, $ifNotExists, lrMixin.options()));
    }

    @Override
    protected final OutputAll execute(RegionCreateResult result) {
        val message = switch (result) {
            case RegionAlreadyExists() -> handleRegionAlreadyExists();
            case RegionCreated() -> handleRegionCreated();
            case RegionCreatedAndDbActive(var waitTime) -> handleRegionCreatedAndDbActive(waitTime);
            case RegionIllegallyAlreadyExists() -> throwRegionAlreadyExists();
        };
        
        return OutputAll.message(trimIndent(message));
    }

    private String handleRegionAlreadyExists() {
        return """
          Region %s already exists in database %s.
        
          %s
          %s
        """.formatted(
            highlight($region),
            highlight($dbRef),
            renderComment("List existing regions:"),
            renderCommand("astra db list-regions %s".formatted($dbRef))
        );
    }

    private String handleRegionCreated() {
        return """
          Region %s has been created in database %s (database may not be active yet).
        
          %s
          %s
        """.formatted(
            highlight($region),
            highlight($dbRef),
            renderComment("Poll the database's status:"),
            renderCommand("astra db status %s".formatted($dbRef))
        );
    }

    private String handleRegionCreatedAndDbActive(Duration waitTime) {
        return """
          Region %s has been created in database %s.
        
          The database is now active after waiting %s seconds.
        """.formatted(
            highlight($region),
            highlight($dbRef),
            highlight(waitTime.toSeconds())
        );
    }

    private String throwRegionAlreadyExists() {
        throw new AstraCliException(REGION_ALREADY_EXISTS, """
          @|bold,red Error: Region %s already exists in database %s.|@
      
          To ignore this error, provide the %s flag to skip this error if the region already exists.

          %s
          %s
    
          %s
          %s
        """.formatted(
            $region,
            $dbRef,
            highlight("--if-not-exists"),
            renderComment("Example fix:"),
            renderCommand(originalArgs(), "--if-not-exists"),
            renderComment("List existing regions:"),
            renderCommand("astra db list-regions %s".formatted($dbRef))
        ));
    }
}
