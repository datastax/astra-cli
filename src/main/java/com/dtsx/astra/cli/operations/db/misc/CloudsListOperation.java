package com.dtsx.astra.cli.operations.db.misc;

import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.dtsx.astra.cli.operations.db.misc.CloudsListOperation.*;

@RequiredArgsConstructor
public class CloudsListOperation implements Operation<Set<String>> {
    private final RegionGateway regionGateway;
    private final CloudsListRequest request;

    public record CloudsListRequest() {}

    @Override
    public Set<String> execute() {
        return regionGateway.findRegionClouds();
    }
}
