package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbGetOperation {
    private final DbGateway dbGateway;

    public record DbGetResult(Database database) {}

    public DbGetResult execute(DbRef dbRef) {
        return new DbGetResult(dbGateway.findOneDb(dbRef));
    }
}
