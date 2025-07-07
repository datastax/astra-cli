package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DbListOperation implements Operation<Stream<Database>> {
    private final DbGateway dbGateway;
    private final DbListRequest request;

    public record DbListRequest(boolean vectorOnly) {}

    @Override
    public Stream<Database> execute() {
        var databases = dbGateway.findAll();

        if (request.vectorOnly) {
            databases = databases.filter(db -> db.getInfo().getDbType() != null);
        }

        return databases;
    }
}
