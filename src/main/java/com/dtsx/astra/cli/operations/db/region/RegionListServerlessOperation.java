package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;

import java.util.SortedMap;

public class RegionListServerlessOperation extends AbstractRegionListOperation {
    public RegionListServerlessOperation(RegionGateway regionGateway, RegionListRequest request) {
        super(regionGateway, request);
    }

    @Override
    public SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> fetchRegions() {
        return regionGateway.findAllServerless(false);
    }
}
