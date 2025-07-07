package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.models.TenantStatus;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class StreamingStatusOperation implements Operation<TenantStatus> {
    private final StreamingGateway streamingGateway;
    private final StreamingStatusRequest request;

    public record StreamingStatusRequest(TenantName tenantName) {}

    @Override
    public TenantStatus execute() {
        return AstraLogger.loading("Fetching status for tenant " + highlight(request.tenantName), (_) -> {
            val streaming = streamingGateway.findOne(request.tenantName);
            return TenantStatus.mkUnsafe(streaming.getStatus());
        });
    }
}
