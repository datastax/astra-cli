package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;

import static com.dtsx.astra.cli.operations.db.DbGetOperation.DbGetResult;

@RequiredArgsConstructor
public class DbGetOperation implements Operation<DbGetResult> {
    private final DbGateway dbGateway;
    private final DbGetRequest request;

    public record DbGetResult(Database database) {}

    public record DbGetRequest(DbRef dbRef) {}

    @Override
    public DbGetResult execute() {
        return new DbGetResult(dbGateway.findOne(request.dbRef));
    }
}
