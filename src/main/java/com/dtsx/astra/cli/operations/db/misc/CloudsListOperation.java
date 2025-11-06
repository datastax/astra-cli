package com.dtsx.astra.cli.operations.db.misc;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.SortedSet;

@RequiredArgsConstructor
public class CloudsListOperation implements Operation<SortedSet<CloudProvider>> {
    private final RegionGateway regionGateway;

    @Override
    public SortedSet<CloudProvider> execute() {
        return regionGateway.findAvailableClouds();
    }
}
