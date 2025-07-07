package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.models.TenantStatus;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.collections.Pair;

import java.util.Optional;

@RequiredArgsConstructor
public class StreamingCreateOperation implements Operation<StreamingCreateOperation.StreamingCreateResult> {
    private final StreamingGateway streamingGateway;
    private final StreamingCreateRequest request;

    public sealed interface StreamingCreateResult {}
    public record TenantAlreadyExistsWithStatus(TenantName tenantName, TenantStatus currStatus) implements StreamingCreateResult {}
    public record TenantAlreadyExistsIllegallyWithStatus(TenantName tenantName, TenantStatus currStatus) implements StreamingCreateResult {}
    public record TenantCreated(TenantName tenantName, TenantStatus currStatus) implements StreamingCreateResult {}

    public record StreamingCreateRequest(
        TenantName tenantName,
        Either<String, Pair<Optional<CloudProviderType>, RegionName>> clusterOrCloud,
        String plan,
        String userEmail,
        boolean ifNotExists
    ) {}

    @Override
    public StreamingCreateResult execute() {
        val status = streamingGateway.create(
            request.tenantName,
            request.clusterOrCloud.map((p) -> Pair.create(
                streamingGateway.findCloudForRegion(p.getLeft(), p.getRight()),
                p.getRight()
            )),
            request.plan,
            request.userEmail
        );

        return switch (status) {
            case CreationStatus.AlreadyExists<Tenant>(var tenant) -> handleExistingTenant(tenant, request);
            case CreationStatus.Created<Tenant>(var tenant) -> handleNewTenant(tenant);
        };
    }

    private StreamingCreateResult handleExistingTenant(Tenant tenant, StreamingCreateRequest request) {
        if (!request.ifNotExists) {
            return new TenantAlreadyExistsIllegallyWithStatus(TenantName.mkUnsafe(tenant.getTenantName()), TenantStatus.mkUnsafe(tenant.getStatus()));
        }

        return new TenantAlreadyExistsWithStatus(TenantName.mkUnsafe(tenant.getTenantName()), TenantStatus.mkUnsafe(tenant.getStatus()));
    }

    private StreamingCreateResult handleNewTenant(Tenant tenant) {
        return new TenantCreated(TenantName.mkUnsafe(tenant.getTenantName()), TenantStatus.mkUnsafe(tenant.getStatus()));
    }
}
