package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.commands.streaming.StreamingGetCmd.StreamingGetKeys;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.models.TenantStatus;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingGetOperation.StreamingInfo;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

@RequiredArgsConstructor
public class StreamingGetOperation implements Operation<StreamingInfo> {
    private final StreamingGateway streamingGateway;
    private final StreamingGetRequest request;

    public record StreamingGetRequest(
        TenantName tenantName,
        Optional<StreamingGetKeys> key
    ) {}

    public sealed interface StreamingInfo {}

    public record StreamingInfoFull(
        TenantStatus status,
        CloudProvider cloud,
        RegionName region,
        String clusterName,
        String pulsarVersion,
        String jvmVersion,
        String webServiceUrl,
        String brokerServiceUrl,
        String webSocketUrl,
        Tenant raw
    ) implements StreamingInfo {}

    public record StreamingInfoValue(
        Object value
    ) implements StreamingInfo {}

    @Override
    public StreamingInfo execute() {
        val tenant = streamingGateway.findOne(request.tenantName);

        return request.key
            .map(key -> mkStreamingInfoValue(key, tenant))
            .orElseGet(() -> mkStreamingInfoFull(tenant));
    }

    private StreamingInfo mkStreamingInfoFull(Tenant tenant) {
        return new StreamingInfoFull(
            status(tenant),
            cloud(tenant),
            region(tenant),
            tenant.getClusterName(),
            tenant.getPulsarVersion(),
            tenant.getJvmVersion(),
            tenant.getWebServiceUrl(),
            tenant.getBrokerServiceUrl(),
            tenant.getWebsocketUrl(),
            tenant
        );
    }

    private StreamingInfo mkStreamingInfoValue(StreamingGetKeys key, Tenant tenant) {
        val value = switch (key) {
            case status -> status(tenant);
            case cloud -> cloud(tenant);
            case pulsar_token -> tenant.getPulsarToken();
            case region -> region(tenant);
        };

        return new StreamingInfoValue(value);
    }

    private TenantStatus status(Tenant tenant) {
        return TenantStatus.mkUnsafe(tenant.getStatus());
    }

    private CloudProvider cloud(Tenant tenant) {
        return CloudProvider.fromString(tenant.getCloudProvider());
    }

    private RegionName region(Tenant tenant) {
        return RegionName.mkUnsafe(tenant.getCloudRegion());
    }
}
