package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DbListOperation {
    private final DbGateway dbGateway;

    public List<Database> execute(boolean vectorOnly) {
        var databases = dbGateway.findAllDbs();

        if (vectorOnly) {
            databases = databases.stream()
                .filter(db -> db.getInfo().getDbType() != null)
                .toList();
        }

        return databases;
    }
}
