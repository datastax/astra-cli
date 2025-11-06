package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;

import java.util.SortedMap;

public class RegionListVectorOperation extends AbstractRegionListOperation {
    public RegionListVectorOperation(RegionGateway regionGateway, RegionListRequest request) {
        super(regionGateway, request);
    }

    @Override
    public SortedMap<CloudProvider, ? extends SortedMap<String, RegionInfo>> fetchRegions() {
        return regionGateway.findAllServerless(true);
    }
}
