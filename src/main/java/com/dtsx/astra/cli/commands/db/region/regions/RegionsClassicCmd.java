package com.dtsx.astra.cli.commands.db.region.regions;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.RegionListRequest;
import com.dtsx.astra.cli.operations.db.region.RegionListClassicOperation;
import picocli.CommandLine.Command;

import java.util.stream.Stream;

@Command(
    name = "classic",
    description = "List all available regions for classic Astra DB databases"
)
@Example(
    comment = "List all available regions for classic Astra DB databases",
    command = "${cli.name} db regions classic"
)
@Example(
    comment = "Filter by cloud provider",
    command = "${cli.name} db regions classic --cloud AWS,GCP"
)
@Example(
    comment = "Filter by partial region name",
    command = "${cli.name} db regions classic --filter us-,ca-"
)
@Example(
    comment = "Filter by zone",
    command = "${cli.name} db regions classic --zone Europe"
)
public class RegionsClassicCmd extends AbstractRegionListCmd {
    @Override
    protected Operation<Stream<FoundRegion>> mkOperation() {
        return new RegionListClassicOperation(regionGateway, new RegionListRequest(
            $nameFilter,
            $cloudFilter,
            $zoneFilter
        ));
    }
}
