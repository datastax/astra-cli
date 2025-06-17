package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.region.RegionDeleteOperation;
import com.dtsx.astra.cli.operations.db.region.RegionDeleteOperation.RegionDeleted;
import com.dtsx.astra.cli.operations.db.region.RegionDeleteOperation.RegionNotFound;
import com.dtsx.astra.cli.operations.db.region.RegionDeleteOperation.RegionDeletedAndDbActive;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "delete-region"
)
public final class RegionDeleteCmd extends AbstractLongRunningRegionRequiredCmd {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if region does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    public OutputAll execute() {
        val result = new RegionDeleteOperation(regionGateway, dbGateway).execute(dbRef, region, ifExists, lrMixin.options());

        return switch (result) {
            case RegionNotFound() -> {
                yield OutputAll.message("Region " + highlight(region) + " does not exist in database " + highlight(dbRef.toString()) + "; nothing to delete");
            }
            case RegionDeleted() -> {
                yield OutputAll.message("Region " + highlight(region) + " has been deleted from database " + highlight(dbRef.toString()) + " (database may not be active yet)");
            }
            case RegionDeletedAndDbActive(var waitTime) -> {
                yield OutputAll.message("Region " + highlight(region) + " has been deleted from database " + highlight(dbRef.toString()) + " (waited " + waitTime.toSeconds() + "s for database to become active)");
            }
        };
    }
}
