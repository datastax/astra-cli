package com.dtsx.astra.cli.operations.db.clone;

import com.dtsx.astra.cli.core.models.CloneOperationId;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.clone.DbCloneGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.DatabaseCloneStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbCloneStatusOperation implements Operation<DatabaseCloneStatus> {
    private final DbCloneGateway dbCloneGateway;
    private final DbCloneStatusRequest request;

    public record DbCloneStatusRequest(DbRef targetDbRef, CloneOperationId operationId) {}

    @Override
    public DatabaseCloneStatus execute() {
        return dbCloneGateway.findClone(request.targetDbRef, request.operationId);
    }
}
