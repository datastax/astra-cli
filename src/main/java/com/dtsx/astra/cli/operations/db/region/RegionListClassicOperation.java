package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.RequiredArgsConstructor;

import java.util.SortedMap;

@RequiredArgsConstructor
public class RegionListClassicOperation {
    private final RegionGateway regionGateway;

    public SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> execute() {
        return regionGateway.findClassicRegions();
    }
}
