package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DbListOperation implements Operation<List<Database>> {
    private final DbGateway dbGateway;
    private final DbListRequest request;

    public record DbListRequest(boolean vectorOnly) {}

    @Override
    public List<Database> execute() {
        var databases = dbGateway.findAllDbs();

        if (request.vectorOnly) {
            databases = databases.stream()
                .filter(db -> db.getInfo().getDbType() != null)
                .toList();
        }

        return databases;
    }
}
