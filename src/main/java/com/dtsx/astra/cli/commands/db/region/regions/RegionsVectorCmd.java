package com.dtsx.astra.cli.commands.db.region.regions;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.RegionListRequest;
import com.dtsx.astra.cli.operations.db.region.RegionListVectorOperation;
import picocli.CommandLine.Command;

import java.util.stream.Stream;

@Command(
    name = "vector",
    description = "List all available regions for vector Astra DB databases"
)
@Example(
    comment = "List all available regions for vector Astra DB databases",
    command = "${cli.name} db regions vector"
)
@Example(
    comment = "Filter by cloud provider",
    command = "${cli.name} db regions vector --cloud AWS,GCP"
)
@Example(
    comment = "Filter by partial region name",
    command = "${cli.name} db regions vector --filter us-,ca-"
)
@Example(
    comment = "Filter by zone",
    command = "${cli.name} db regions vector --zone Europe"
)
public class RegionsVectorCmd extends AbstractRegionListCmd {
    @Override
    protected Operation<Stream<FoundRegion>> mkOperation() {
        return new RegionListVectorOperation(regionGateway, new RegionListRequest(
            $nameFilter,
            $cloudFilter,
            $zoneFilter
        ));
    }
}
