package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.region.RegionCreateOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_DESC;
import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LR_OPTS_TIMEOUT_NAME;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.region.RegionCreateOperation.*;

@Command(
    name = "create-region"
)
public class RegionCreateCmd extends AbstractLongRunningRegionRequiredCmd<RegionCreateResult> {
    @Option(
        names = { "--if-not-exists" },
        description = { "Will create a new region only if none with same name", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifNotExists;

    @Option(names = LR_OPTS_TIMEOUT_NAME, description = LR_OPTS_TIMEOUT_DESC, defaultValue = "600")
    public void setTimeout(int timeout) {
        lrMixin.setTimeout(timeout);
    }

    @Override
    protected RegionCreateOperation mkOperation() {
        return new RegionCreateOperation(regionGateway, dbGateway, new RegionCreateRequest(dbRef, region, ifNotExists, lrMixin.options()));
    }

    @Override
    protected final OutputAll execute(RegionCreateResult result) {
        return switch (result) {
            case RegionAlreadyExists() -> {
                yield OutputAll.message("Region " + highlight(region) + " already exists in database " + highlight(dbRef.toString()));
            }
            case RegionCreated() -> {
                yield OutputAll.message("Region " + highlight(region) + " has been created in database " + highlight(dbRef.toString()) + " (database may not be active yet)");
            }
            case RegionCreatedAndDbActive(var waitTime) -> {
                yield OutputAll.message("Region " + highlight(region) + " has been created in database " + highlight(dbRef.toString()) + 
                    " (waited " + waitTime.toSeconds() + "s for database to become active)");
            }
            case RegionIllegallyAlreadyExists() -> {
                throw new RegionCreateOperation.RegionAlreadyExistsException(region, dbRef);
            }
        };
    }
}
