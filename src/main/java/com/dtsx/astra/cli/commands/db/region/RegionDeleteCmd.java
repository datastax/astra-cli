package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.region.RegionDeleteOperation;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.REGION_NOT_FOUND;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.region.RegionDeleteOperation.*;

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
    protected final OutputAll execute(Supplier<RegionDeleteResult> result) {
        return switch (result.get()) {
            case RegionNotFound() -> handleRegionNotFound();
            case RegionDeleted() -> handleRegionDeleted();
            case RegionDeletedAndDbActive(var waitTime) -> handleRegionDeletedAndDbActive(waitTime);
            case RegionIllegallyNotFound() -> throwRegionNotFound();
        };
    }

    private OutputAll handleRegionNotFound() {
        val message = "Region %s does not exist in database %s; nothing to delete.".formatted(
            highlight($region),
            highlight($dbRef)
        );

        val data = mkData(false, null);

        return OutputAll.response(message, data, List.of(
            new Hint("List all regions in the database:", "astra db list-regions %s".formatted($dbRef)),
            new Hint("Get database information:", "astra db get %s".formatted($dbRef))
        ));
    }

    private OutputAll handleRegionDeleted() {
        val message = "Region %s has been deleted from database %s (database may not be active yet).".formatted(
            highlight($region),
            highlight($dbRef)
        );

        val data = mkData(true, null);

        return OutputAll.response(message, data, List.of(
            new Hint("Poll the database's status:", "astra db status %s".formatted($dbRef)),
            new Hint("List remaining regions in the database:", "astra db list-regions %s".formatted($dbRef))
        ));
    }

    private OutputAll handleRegionDeletedAndDbActive(Duration waitTime) {
        val message = "Region %s has been deleted from database %s. Database is now active after waiting %s seconds.".formatted(
            highlight($region),
            highlight($dbRef),
            highlight(waitTime.toSeconds())
        );

        val data = mkData(true, waitTime);

        return OutputAll.response(message, data, List.of(
            new Hint("List remaining regions in the database:", "astra db list-regions %s".formatted($dbRef))
        ));
    }

    private <T> T throwRegionNotFound() {
        throw new AstraCliException(REGION_NOT_FOUND, """
          @|bold,red Error: Region %s does not exist in database %s.|@
    
          To ignore this error, provide the @!--if-exists!@ flag to skip this error if the region doesn't exist.
        """.formatted(
            $region,
            $dbRef
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("List existing regions:", "astra db list-regions %s".formatted($dbRef))
        ));
    }

    private Map<String, Object> mkData(Boolean wasDeleted, @Nullable Duration waitedDuration) {
        return Map.of(
            "wasDeleted", wasDeleted,
            "waitedSeconds", Optional.ofNullable(waitedDuration).map(Duration::getSeconds)
        );
    }
}
