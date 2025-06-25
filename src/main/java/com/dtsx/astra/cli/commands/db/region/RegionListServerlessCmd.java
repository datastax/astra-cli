package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.RegionListServerlessOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import picocli.CommandLine.Command;

import java.util.SortedMap;

@Command(
    name = "list-regions-serverless"
)
public class RegionListServerlessCmd extends AbstractRegionListCmd {
    @Override
    protected Operation<SortedMap<CloudProviderType,? extends SortedMap<String, RegionInfo>>> mkOperation() {
        return new RegionListServerlessOperation(regionGateway);
    }
}
