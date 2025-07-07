package com.dtsx.astra.cli.operations.streaming.pulsar;

import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamingPulsarTokenOperation implements Operation<String> {
    private final StreamingGateway streamingGateway;
    private final PulsarTokenRequest request;

    public record PulsarTokenRequest(
        TenantName tenantName
    ) {}

    @Override
    public String execute() {
        return streamingGateway.findOne(request.tenantName).getPulsarToken();
    }
}
