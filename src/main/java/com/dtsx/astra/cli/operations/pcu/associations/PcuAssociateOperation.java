package com.dtsx.astra.cli.operations.pcu.associations;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuAssocTarget;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuAssociateOperation.AssociationCreateResult;
import com.dtsx.astra.cli.utils.DbUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class PcuAssociateOperation implements Operation<AssociationCreateResult> {
    private final DbGateway dbGateway;
    private final PcuAssociationsGateway associationsGateway;
    private final PcuAssociateRequest request;

    public record PcuAssociateRequest(
        PcuRef ref,
        PcuAssocTarget target,
        boolean ifNotExists
    ) {}

    public sealed interface AssociationCreateResult {}
    public record AssociationAlreadyExists(DatacenterId dc) implements AssociationCreateResult {}
    public record AssociationIllegallyAlreadyExists(DatacenterId dc) implements AssociationCreateResult {}
    public record AssociationCreated(DatacenterId dc) implements AssociationCreateResult {}

    @Override
    public AssociationCreateResult execute() {
        val dcId = DbUtils.resolvePcuAssocTarget(dbGateway, request.target);

        val status = associationsGateway.create(
            request.ref,
            dcId
        );

        return switch (status) {
            case CreationStatus.Created<?> _ -> new AssociationCreated(dcId);
            case CreationStatus.AlreadyExists<?> _ -> handleAssocAlreadyExists(dcId, request.ifNotExists);
        };
    }

    private AssociationCreateResult handleAssocAlreadyExists(DatacenterId dcId, boolean ifNotExists) {
        if (ifNotExists) {
            return new AssociationAlreadyExists(dcId);
        } else {
            return new AssociationIllegallyAlreadyExists(dcId);
        }
    }
}
