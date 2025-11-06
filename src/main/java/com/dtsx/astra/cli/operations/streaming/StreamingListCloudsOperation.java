package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.SortedSet;

@RequiredArgsConstructor
public class StreamingListCloudsOperation implements Operation<SortedSet<CloudProvider>> {
    private final StreamingGateway streamingGateway;

    @Override
    public SortedSet<CloudProvider> execute() {
        return streamingGateway.findAvailableClouds();
    }
}
