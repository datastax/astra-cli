package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.models.PcuStatus;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuUpdateOperation.PcuUpdateResult;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PcuUpdateOperation implements Operation<PcuUpdateResult> {
    private final PcuGateway pcuGateway;
    private final UpdatePcuRequest request;

    public record UpdatePcuRequest(
        PcuRef pcuRef,
        Optional<String> title,
        Optional<String> description,
        Optional<CloudProvider> cloud,
        Optional<RegionName> region,
        Optional<Integer> min,
        Optional<Integer> max,
        Optional<Integer> reserved,
        boolean allowDuplicates
    ) {}

    public sealed interface PcuUpdateResult {}
    public record PcuGroupAlreadyExistsIllegallyWithStatus(UUID pcuId, PcuStatus currStatus) implements PcuUpdateResult {}
    public record PcuGroupUpdated() implements PcuUpdateResult {}

    @Override
    public PcuUpdateResult execute() {
        val builder = PCUGroupUpdateRequest.builder()
            .title(request.title.orElse(null))
            .description(request.description.orElse(null))
            .cloudProvider(request.cloud.map(CloudProvider::toSdkType).orElse(null))
            .region(request.region.map(RegionName::unwrap).orElse(null))
            .min(request.min.orElse(null))
            .max(request.max.orElse(null))
            .reserved(request.reserved.orElse(null))
            .build();

        val status = pcuGateway.update(
            request.pcuRef,
            builder,
            request.allowDuplicates
        );

        val pcu = status.value();

        return switch (status) {
            case CreationStatus.Created<?> _ -> new PcuUpdateOperation.PcuGroupUpdated();
            case CreationStatus.AlreadyExists<?> _ -> new PcuGroupAlreadyExistsIllegallyWithStatus(pcu.getId(), new PcuStatus(pcu));
        };
    }
}
