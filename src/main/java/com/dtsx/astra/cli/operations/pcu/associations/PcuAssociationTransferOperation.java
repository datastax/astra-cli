package com.dtsx.astra.cli.operations.pcu.associations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuAssocTarget;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociationTransferOperation.AssociationTransferResult;
import com.dtsx.astra.cli.utils.DbUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PcuAssociationTransferOperation implements Operation<AssociationTransferResult> {
    private final CliContext ctx;
    private final DbGateway dbGateway;
    private final PcuGateway pcuGateway;
    private final PcuAssociationsGateway associationsGateway;
    private final PcuTransferRequest request;

    public sealed interface SourceBehavior {}
    public record AutodetectAndRequireSource() implements SourceBehavior {}
    public record AutodetectSourceOrCreate() implements SourceBehavior {}
    public record UseSource(PcuRef from) implements SourceBehavior {}

    public record PcuTransferRequest(
        PcuRef to,
        PcuAssocTarget target,
        SourceBehavior sourceBehavior,
        boolean ifNotExists
    ) {}

    public sealed interface AssociationTransferResult {}
    public record AssociationTransferred(UUID from, PcuRef fromRef, UUID toId, DatacenterId target) implements AssociationTransferResult {}
    public record AssociationCreated(UUID toId, DatacenterId target) implements AssociationTransferResult {}
    public record AlreadyAssociatedToTarget(UUID toId, DatacenterId target) implements AssociationTransferResult {}
    public record IllegallyAlreadyAssociatedToTarget(UUID toId, DatacenterId target) implements AssociationTransferResult {}
    public record CouldNotDetectSource(DatacenterId target) implements AssociationTransferResult {}
    public record GivenSourceNotAssociated(UUID givenSource, Optional<UUID> actualSource, DatacenterId target) implements AssociationTransferResult {}

    @Override
    public AssociationTransferResult execute() {
        val dcId = DbUtils.resolvePcuAssocTarget(dbGateway, request.target);

        val toPcuId = UUID.fromString(
            pcuGateway.findOne(request.to).getId()
        );

//        val fromPcuId = associationsGateway.tryFindByDatacenter(dcId)
//            .map(PcuGroupDatacenterAssociation::getPcuGroupUUID)
//            .map(UUID::fromString);

        Optional<UUID> fromPcuId = switch (request.sourceBehavior) {
            case AutodetectAndRequireSource(), AutodetectSourceOrCreate() -> Optional.empty();
            case UseSource(var ref) -> Optional.of(UUID.fromString(pcuGateway.findOne(ref).getId()));
        };

        if (fromPcuId.isPresent() && fromPcuId.get().equals(toPcuId)) {
            return alreadyExists(toPcuId, dcId);
        }

        return switch (request.sourceBehavior) {
            case AutodetectAndRequireSource() -> requireDetectedSource(fromPcuId, toPcuId, dcId);
            case AutodetectSourceOrCreate() -> detectOrCreateSource(fromPcuId, toPcuId, dcId);
            case UseSource(var source) -> useSource(source, fromPcuId, toPcuId, dcId);
        };
    }

    private AssociationTransferResult requireDetectedSource(Optional<UUID> fromPcuId, UUID toPcuId, DatacenterId dcId) {
        if (fromPcuId.isEmpty()) {
            return new CouldNotDetectSource(dcId);
        }

        return transfer(fromPcuId.get(), null, toPcuId, dcId);
    }

    private AssociationTransferResult detectOrCreateSource(Optional<UUID> fromPcuId, UUID toPcuId, DatacenterId dcId) {
        if (fromPcuId.isEmpty()) {
            associationsGateway.create(
                PcuRef.fromId(toPcuId),
                dcId
            );
            return new AssociationCreated(toPcuId, dcId);
        }

        return transfer(fromPcuId.get(), null, toPcuId, dcId);
    }

    private AssociationTransferResult useSource(PcuRef givenSource, Optional<UUID> actualSource, UUID toPcuId, DatacenterId dcId) {
        Optional<AssociationTransferResult> res = ctx.log().loading("Verifying if @!%s!@ is actually associated to @!%s!@".formatted(givenSource, request.target), (_) -> {
            val givenSourceId = UUID.fromString(
                pcuGateway.findOne(givenSource).getId()
            );

            if (actualSource.isEmpty()) {
                return Optional.of(new GivenSourceNotAssociated(givenSourceId, Optional.empty(), dcId));
            }

            if (!actualSource.get().equals(givenSourceId)) {
                return Optional.of(new GivenSourceNotAssociated(givenSourceId, actualSource, dcId));
            }

            return Optional.empty();
        });

        return res.orElseGet(() -> (
           transfer(actualSource.orElseThrow(), givenSource, toPcuId, dcId)
        ));
    }

    private AssociationTransferResult transfer(UUID from, @Nullable PcuRef fromRef, UUID to, DatacenterId dcId) {
        associationsGateway.transfer(from, to, dcId);

        return new AssociationTransferred(
            from,
            (fromRef != null) ? fromRef : PcuRef.fromId(from),
            to,
            dcId
        );
    }

    private AssociationTransferResult alreadyExists(UUID to, DatacenterId dcId) {
        if (request.ifNotExists) {
            return new AlreadyAssociatedToTarget(to, dcId);
        } else {
            return new IllegallyAlreadyAssociatedToTarget(to, dcId);
        }
    }
}
