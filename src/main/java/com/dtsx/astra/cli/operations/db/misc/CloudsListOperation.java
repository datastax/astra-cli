package com.dtsx.astra.cli.operations.db.misc;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class CloudsListOperation {
    private final RegionGateway regionGateway;

    public Set<String> execute() {
        return regionGateway.findRegionClouds();
    }
}
