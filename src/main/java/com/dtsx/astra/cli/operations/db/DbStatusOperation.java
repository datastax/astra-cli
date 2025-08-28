package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class DbStatusOperation implements Operation<DatabaseStatusType> {
    private final CliContext ctx;
    private final DbGateway dbGateway;
    private final DbStatusRequest request;

    public record DbStatusRequest(DbRef dbRef) {}

    @Override
    public DatabaseStatusType execute() {
        return ctx.log().loading("Fetching status for database " + ctx.highlight(request.dbRef), (_) -> {
            val db = dbGateway.findOne(request.dbRef);
            return db.getStatus();
        });
    }
}
