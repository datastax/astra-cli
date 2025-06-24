package com.dtsx.astra.cli.commands.db.endpoints;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.exceptions.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import picocli.CommandLine.Option;

import java.util.Optional;

public abstract class AbstractEndpointGetCmd extends AbstractDbSpecificCmd {
    @Option(
        names = { "--region", "-r" },
        description = { "The region to use" },
        paramLabel = "REGION"
    )
    protected Optional<RegionName> region;

    protected abstract String extractEndpoint(Database db, RegionName region, AstraEnvironment env);

    @Override
    protected OutputAll execute() {
        val db = dbGateway.findOneDb(dbRef);

        if (region.isPresent()) {
            val regionIsValid = db.getInfo().getDatacenters().stream().anyMatch(dc -> dc.getRegion().equalsIgnoreCase(region.get().unwrap()));

            if (!regionIsValid) {
                throw new RegionNotFoundException(dbRef, region.get());
            }
        }

        return OutputAll.serializeValue(extractEndpoint(db, region.orElse(RegionName.mkUnsafe(db.getInfo().getRegion())), profile().env()));
    }
}
