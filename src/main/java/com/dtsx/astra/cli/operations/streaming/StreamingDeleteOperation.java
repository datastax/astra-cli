package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class StreamingDeleteOperation implements Operation<StreamingDeleteOperation.StreamingDeleteResult> {
    private final StreamingGateway streamingGateway;
    private final StreamingDeleteRequest request;

    public sealed interface StreamingDeleteResult {}
    public record TenantNotFound() implements StreamingDeleteResult {}
    public record TenantIllegallyNotFound() implements StreamingDeleteResult {}
    public record TenantDeleted() implements StreamingDeleteResult {}

    public record StreamingDeleteRequest(TenantName tenantName, boolean ifExists) {}

    @Override
    public StreamingDeleteResult execute() {
        val status = streamingGateway.delete(request.tenantName);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleTenantDeleted();
            case DeletionStatus.NotFound<?> _ -> handleTenantNotFound(request.ifExists);
        };
    }

    private StreamingDeleteResult handleTenantDeleted() {
        return new TenantDeleted();
    }

    private StreamingDeleteResult handleTenantNotFound( boolean ifExists) {
        if (ifExists) {
            return new TenantNotFound();
        } else {
            return new TenantIllegallyNotFound();
        }
    }
}
