package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.region.RegionCreateOperation;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.ExitCode.REGION_ALREADY_EXISTS;
import static com.dtsx.astra.cli.operations.db.region.RegionCreateOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "create-region",
    description = "Add a new region to an existing database"
)
@Example(
    comment = "Create a region in a database",
    command = "${cli.name} db create-region my_db -r us-west-2"
)
@Example(
    comment = "Create a region without failing if it already exists",
    command = "${cli.name} db create-region my_db -r us-west-2 --if-not-exists"
)
@Example(
    comment = "Create a region without waiting for the database to become active",
    command = "${cli.name} db create-region my_db -r us-west-2 --async"
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
    protected final OutputAll execute(Supplier<RegionCreateResult> result) {
        return switch (result.get()) {
            case RegionAlreadyExists() -> handleRegionAlreadyExists();
            case RegionCreated() -> handleRegionCreated();
            case RegionCreatedAndDbActive(var waitTime) -> handleRegionCreatedAndDbActive(waitTime);
            case RegionIllegallyAlreadyExists() -> throwRegionAlreadyExists();
        };
    }

    private OutputAll handleRegionAlreadyExists() {
        val message = "Region %s already exists in database %s.".formatted(
            ctx.highlight($region),
            ctx.highlight($dbRef)
        );

        val data = mkData(false, null);

        return OutputAll.response(message, data, List.of(
            new Hint("List existing regions:", "${cli.name} db list-regions %s".formatted($dbRef))
        ));
    }

    private OutputAll handleRegionCreated() {
        val message = "Region %s has been created in database %s (database may not be active yet).".formatted(
            ctx.highlight($region),
            ctx.highlight($dbRef)
        );

        val data = mkData(true, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the database's status:", "${cli.name} db status %s".formatted($dbRef))
        ));
    }

    private OutputAll handleRegionCreatedAndDbActive(Duration waitTime) {
        val message = "Region %s has been created in database %s. The database is now active after waiting %s seconds.".formatted(
            ctx.highlight($region),
            ctx.highlight($dbRef),
            ctx.highlight(waitTime.toSeconds())
        );

        val data = mkData(true, waitTime);

        return OutputAll.response(message, data);
    }

    private <T> T throwRegionAlreadyExists() {
        throw new AstraCliException(REGION_ALREADY_EXISTS, """
          @|bold,red Error: Region %s already exists in database %s.|@
      
          To ignore this error, provide the @!--if-not-exists!@ flag to skip this error if the region already exists.
        """.formatted(
            $region,
            $dbRef
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-not-exists"),
            new Hint("List existing regions:", "${cli.name} db list-regions %s".formatted($dbRef))
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasCreated, @Nullable Duration waitedDuration) {
        return sequencedMapOf(
            "wasCreated", wasCreated,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }
}
