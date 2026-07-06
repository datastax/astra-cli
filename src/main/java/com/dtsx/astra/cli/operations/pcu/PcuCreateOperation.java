package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.PcuStatus;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.sdk.pcu.domain.PCUGroupCreationRequest;
import com.dtsx.astra.sdk.pcu.domain.PCUProvisionType;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.UUID;

import static com.dtsx.astra.cli.operations.pcu.PcuCreateOperation.PcuCreateResult;

@RequiredArgsConstructor
public class PcuCreateOperation implements Operation<PcuCreateResult> {
    private final PcuGateway pcuGateway;
    private final CreatePcuRequest request;

    public enum ExistingBehavior {
        FAIL,
        CREATE_IF_NOT_EXISTS,
        ALLOW_DUPLICATES
    }

    public record CreatePcuRequest(
        String title,
        Optional<String> description,
        CloudProvider cloud,
        RegionName region,
        String instanceType,
        PCUProvisionType provisionType,
        Integer min,
        Integer max,
        Integer reserved,
        ExistingBehavior existingBehavior
    ) {}

    public sealed interface PcuCreateResult {}
    public record PcuGroupAlreadyExistsWithStatus(UUID pcuId, PcuStatus currStatus) implements PcuCreateResult {}
    public record PcuGroupAlreadyExistsIllegallyWithStatus(UUID pcuId, PcuStatus currStatus) implements PcuCreateResult {}
    public record PcuGroupCreated(UUID pcuId, PcuStatus pcuStatus) implements PcuCreateResult {}

    @Override
    public PcuCreateResult execute() {
        val builder = PCUGroupCreationRequest.builder()
            .title(request.title)
            .description(request.description.orElse(null))
            .instanceType(request.instanceType)
            .provisionType(request.provisionType.name())
            .cloudProvider(request.cloud.toSdkType())
            .region(request.region.unwrap())
            .min(request.min)
            .max(request.max)
            .reserved(request.reserved)
            .build();

        val status = pcuGateway.create(
            request.title,
            builder,
            request.existingBehavior == ExistingBehavior.ALLOW_DUPLICATES
        );

        val pcu = status.value();

        if (status instanceof CreationStatus.Created<?>) {
            return new PcuCreateOperation.PcuGroupCreated(pcu.getId(), new PcuStatus(pcu));
        }

        if (request.existingBehavior == ExistingBehavior.FAIL) {
            return new PcuGroupAlreadyExistsIllegallyWithStatus(pcu.getId(), new PcuStatus(pcu));
        }

        return new PcuCreateOperation.PcuGroupAlreadyExistsWithStatus(pcu.getId(), new PcuStatus(pcu));
    }
}
