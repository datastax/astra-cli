package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.region.RegionListClassicOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import picocli.CommandLine.Command;

import java.util.SortedMap;

import static com.dtsx.astra.cli.operations.db.region.RegionListClassicOperation.RegionListClassicRequest;

@Command(
    name = "list-regions-classic"
)
public class RegionListClassicCmd extends AbstractRegionListCmd {
    @Override
    protected Operation<SortedMap<CloudProviderType,? extends SortedMap<String, RegionInfo>>> mkOperation() {
        return new RegionListClassicOperation(regionGateway, new RegionListClassicRequest());
    }
}
