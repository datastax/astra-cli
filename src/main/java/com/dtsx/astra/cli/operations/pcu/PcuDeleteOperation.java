package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuDeleteOperation.PcuDeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.UUID;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class PcuDeleteOperation implements Operation<PcuDeleteResult> {
    private final PcuGateway pcuGateway;
    private final PcuDeleteRequest request;

    public sealed interface PcuDeleteResult {}
    public record PcuNotFound() implements PcuDeleteResult {}
    public record PcuDeleted() implements PcuDeleteResult {}
    public record PcuIllegallyNotFound() implements PcuDeleteResult {}

    public record PcuDeleteRequest(
        PcuRef pcuRef,
        boolean ifExists,
        boolean forceDelete,
        BiConsumer<String, UUID> assertShouldDelete
    ) {}

    @Override
    public PcuDeleteResult execute() {
        if (!request.forceDelete) {
            val pcuInfo = pcuGateway.tryFindOne(request.pcuRef);

            if (pcuInfo.isEmpty()) {
                return handlePcuNotFound(request.ifExists);
            }

            request.assertShouldDelete.accept(pcuInfo.get().getTitle(), UUID.fromString(pcuInfo.get().getId()));
        }

        val status = pcuGateway.delete(request.pcuRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> new PcuDeleted();
            case DeletionStatus.NotFound<?> _ -> handlePcuNotFound(request.ifExists);
        };
    }

    private PcuDeleteResult handlePcuNotFound(boolean ifExists) {
        if (ifExists) {
            return new PcuNotFound();
        } else {
            return new PcuIllegallyNotFound();
        }
    }
}
