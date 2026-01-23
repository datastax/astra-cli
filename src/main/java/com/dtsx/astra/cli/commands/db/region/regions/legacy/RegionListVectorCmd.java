package com.dtsx.astra.cli.commands.db.region.regions.legacy;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.RegionListRequest;
import com.dtsx.astra.cli.operations.db.region.RegionListVectorOperation;
import picocli.CommandLine.Command;

import java.util.stream.Stream;

@Command(
    name = "list-regions-vector",
    description = "List all available regions for vector Astra DB databases",
    hidden = true
)
public class RegionListVectorCmd extends AbstractLegacyRegionListCmd {
    @Override
    protected Operation<Stream<FoundRegion>> mkOperation() {
        return new RegionListVectorOperation(regionGateway, new RegionListRequest(
            $nameFilter,
            $cloudFilter,
            $zoneFilter
        ));
    }
}
