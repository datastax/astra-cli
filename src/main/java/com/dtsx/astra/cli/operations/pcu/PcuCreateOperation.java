package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupCreationRequest;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupProvisionType;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
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
        CloudProviderType cloud,
        RegionName region,
        String instanceType,
        PcuGroupProvisionType provisionType,
        Integer min,
        Integer max,
        Integer reserved,
        ExistingBehavior existingBehavior
    ) {}

    public sealed interface PcuCreateResult {}
    public record PcuGroupAlreadyExistsWithStatus(UUID pcuId, PcuGroupStatusType currStatus) implements PcuCreateResult {}
    public record PcuGroupAlreadyExistsIllegallyWithStatus(UUID pcuId, PcuGroupStatusType currStatus) implements PcuCreateResult {}
    public record PcuGroupCreated(UUID pcuId) implements PcuCreateResult {}

    @Override
    public PcuCreateResult execute() {
        val builder = PcuGroupCreationRequest.builder()
            .title(request.title)
            .description(request.description.orElse(null))
            .instanceType(request.instanceType)
            .provisionType(request.provisionType)
            .cloudProvider(request.cloud)
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
        val pcuId = UUID.fromString(pcu.getId());

        if (status instanceof CreationStatus.Created<?>) {
            return new PcuCreateOperation.PcuGroupCreated(pcuId);
        }

        if (request.existingBehavior == ExistingBehavior.FAIL) {
            return new PcuGroupAlreadyExistsIllegallyWithStatus(pcuId, pcu.getStatus());
        }

        return new PcuCreateOperation.PcuGroupAlreadyExistsWithStatus(pcuId, pcu.getStatus());
    }
}
