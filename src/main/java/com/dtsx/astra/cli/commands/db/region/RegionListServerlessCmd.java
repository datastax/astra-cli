package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.operations.db.region.RegionListServerlessOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import picocli.CommandLine.Command;

import java.util.SortedMap;
import java.util.function.Supplier;

@Command(
    name = "list-regions-serverless"
)
public final class RegionListServerlessCmd extends AbstractRegionListCmd {
    @Override
    protected Supplier<? extends SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>>> getRegionListMethod() {
        return new RegionListServerlessOperation(regionGateway)::execute;
    }
}
