package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.RequiredArgsConstructor;

import java.util.SortedMap;

import static com.dtsx.astra.cli.operations.db.region.RegionListServerlessOperation.*;

@RequiredArgsConstructor
public class RegionListServerlessOperation implements Operation<SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>>> {
    private final RegionGateway regionGateway;
    private final RegionListServerlessRequest request;

    public record RegionListServerlessRequest() {}

    @Override
    public SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> execute() {
        return regionGateway.findServerlessRegions(false);
    }
}
