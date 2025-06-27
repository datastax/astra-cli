package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.RegionListClassicOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import picocli.CommandLine.Command;

import java.util.SortedMap;

@Command(
    name = "list-regions-classic",
    description = "List all available regions for classic Astra DB databases"
)
@Example(
    comment = "List all available regions for classic Astra DB databases",
    command = "astra db region list-regions-classic"
)
@Example(
    comment = "Filter by cloud provider",
    command = "astra db region list-regions-classic --cloud AWS,GCP"
)
@Example(
    comment = "Filter by partial region name",
    command = "astra db region list-regions-classic --filter us-,ca-"
)
@Example(
    comment = "Filter by zone",
    command = "astra db region list-regions-classic --zone Europe"
)
public class RegionListClassicCmd extends AbstractRegionListCmd {
    @Override
    protected Operation<SortedMap<CloudProviderType,? extends SortedMap<String, RegionInfo>>> mkOperation() {
        return new RegionListClassicOperation(regionGateway);
    }
}
