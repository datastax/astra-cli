package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class StreamingListCloudsOperation implements Operation<Set<CloudProviderType>> {
    private final StreamingGateway streamingGateway;

    @Override
    public Set<CloudProviderType> execute() {
        return streamingGateway.findAvailableClouds();
    }
}
