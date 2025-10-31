package com.dtsx.astra.cli.operations.pcu.associations;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DatacenterId;
import com.dtsx.astra.cli.core.models.PcuAssocTarget;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.associations.PcuDisassociateOperation.AssociationDeleteResult;
import com.dtsx.astra.cli.utils.DbUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class PcuDisassociateOperation implements Operation<AssociationDeleteResult> {
    private final DbGateway dbGateway;
    private final PcuAssociationsGateway associationsGateway;
    private final PcuDisassociateRequest request;

    public record PcuDisassociateRequest(
        PcuRef ref,
        PcuAssocTarget target,
        boolean ifExists
    ) {}

    public sealed interface AssociationDeleteResult {}
    public record AssociationNotFound(DatacenterId dc) implements AssociationDeleteResult {}
    public record AssociationIllegallyNotFound(DatacenterId dc) implements AssociationDeleteResult {}
    public record AssociationDeleted(DatacenterId dc) implements AssociationDeleteResult {}

    @Override
    public AssociationDeleteResult execute() {
        val dcId = DbUtils.resolvePcuAssocTarget(dbGateway, request.target);

        val status = associationsGateway.delete(
            request.ref,
            dcId
        );

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> new AssociationDeleted(dcId);
            case DeletionStatus.NotFound<?> _ -> handleAssocNotFound(dcId, request.ifExists);
        };
    }

    private AssociationDeleteResult handleAssocNotFound(DatacenterId dcId, boolean ifNotExists) {
        if (ifNotExists) {
            return new AssociationNotFound(dcId);
        } else {
            return new AssociationIllegallyNotFound(dcId);
        }
    }
}
