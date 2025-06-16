package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.operations.db.region.RegionListClassicOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import picocli.CommandLine.Command;

import java.util.SortedMap;
import java.util.function.Supplier;

@Command(
    name = "list-regions-classic"
)
public final class RegionListClassicCmd extends AbstractRegionListCmd {
    @Override
    protected Supplier<? extends SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>>> getRegionListMethod() {
        return new RegionListClassicOperation(regionGateway)::execute;
    }
}
