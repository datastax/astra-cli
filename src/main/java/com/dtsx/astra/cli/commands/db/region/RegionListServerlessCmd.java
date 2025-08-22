package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.RegionListRequest;
import com.dtsx.astra.cli.operations.db.region.RegionListServerlessOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import picocli.CommandLine.Command;

import java.util.SortedMap;
import java.util.stream.Stream;

@Command(
    name = "list-regions-serverless",
    description = "List all available regions for serverless Astra DB databases"
    )
@Example(
    comment = "List all available regions for serverless Astra DB databases",
    command = "${cli.name} db region list-regions-serverless"
)
@Example(
    comment = "Filter by cloud provider",
    command = "${cli.name} db region list-regions-serverless --cloud AWS,GCP"
)
@Example(
    comment = "Filter by partial region name",
    command = "${cli.name} db region list-regions-serverless --filter us-,ca-"
)
@Example(
    comment = "Filter by zone",
    command = "${cli.name} db region list-regions-serverless --zone Europe"
)
public class RegionListServerlessCmd extends AbstractRegionListCmd {
    @Override
    protected Operation<Stream<FoundRegion>> mkOperation() {
        return new RegionListServerlessOperation(regionGateway, new RegionListRequest(
            $nameFilter,
            $cloudFilter,
            $zoneFilter
        ));
    }
}
