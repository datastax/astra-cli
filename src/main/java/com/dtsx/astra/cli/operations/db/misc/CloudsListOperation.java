package com.dtsx.astra.cli.operations.db.misc;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class CloudsListOperation implements Operation<Set<String>> {
    private final RegionGateway regionGateway;

    @Override
    public Set<String> execute() {
        return regionGateway.findRegionClouds();
    }
}
