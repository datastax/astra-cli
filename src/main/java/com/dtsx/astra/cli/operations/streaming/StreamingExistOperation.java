package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamingExistOperation implements Operation<Boolean> {
    private final StreamingGateway streamingGateway;
    private final StreamingExistRequest request;

    public record StreamingExistRequest(TenantName tenantName) {}

    @Override
    public Boolean execute() {
        return streamingGateway.exists(request.tenantName);
    }
}
