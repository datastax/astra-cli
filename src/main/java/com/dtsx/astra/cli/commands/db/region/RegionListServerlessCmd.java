package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.RegionListServerlessOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import picocli.CommandLine.Command;

import java.util.SortedMap;

@Command(
    name = "list-regions-serverless",
    description = "List all available regions for serverless Astra DB databases"
    )
@Example(
    comment = "List all available regions for serverless Astra DB databases",
    command = "astra db region list-regions-serverless"
)
@Example(
    comment = "Filter by cloud provider",
    command = "astra db region list-regions-serverless --cloud AWS,GCP"
)
@Example(
    comment = "Filter by partial region name",
    command = "astra db region list-regions-serverless --filter us-,ca-"
)
@Example(
    comment = "Filter by zone",
    command = "astra db region list-regions-serverless --zone Europe"
)
public class RegionListServerlessCmd extends AbstractRegionListCmd {
    @Override
    protected Operation<SortedMap<CloudProviderType,? extends SortedMap<String, RegionInfo>>> mkOperation() {
        return new RegionListServerlessOperation(regionGateway);
    }
}
