package com.dtsx.astra.cli.operations.db.misc;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class CloudsListOperation implements Operation<Set<CloudProviderType>> {
    private final RegionGateway regionGateway;

    @Override
    public Set<CloudProviderType> execute() {
        return regionGateway.findAvailableClouds();
    }
}
