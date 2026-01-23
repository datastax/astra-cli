package com.dtsx.astra.cli.commands.db.region.regions.legacy;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.RegionListRequest;
import com.dtsx.astra.cli.operations.db.region.RegionListClassicOperation;
import picocli.CommandLine.Command;

import java.util.stream.Stream;

@Command(
    name = "list-regions-classic",
    description = "List all available regions for classic Astra DB databases",
    hidden = true
)
public class RegionListClassicCmd extends AbstractLegacyRegionListCmd {
    @Override
    protected Operation<Stream<FoundRegion>> mkOperation() {
        return new RegionListClassicOperation(regionGateway, new RegionListRequest(
            $nameFilter,
            $cloudFilter,
            $zoneFilter
        ));
    }
}
