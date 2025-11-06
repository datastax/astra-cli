package com.dtsx.astra.cli.operations.streaming;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.TenantStatus;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingListOperation.TenantInfo;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class StreamingListOperation implements Operation<Stream<TenantInfo>> {
    private final StreamingGateway streamingGateway;

    public record TenantInfo(
        String name,
        CloudProvider cloud,
        RegionName region,
        TenantStatus status,
        Tenant raw
    ) {}

    @Override
    public Stream<TenantInfo> execute() {
        return streamingGateway.findAll().map((raw) -> new TenantInfo(
            raw.getTenantName(),
            CloudProvider.fromString(raw.getCloudProvider()),
            RegionName.mkUnsafe(raw.getCloudRegion()),
            TenantStatus.mkUnsafe(raw.getStatus()),
            raw
        ));
    }
}
