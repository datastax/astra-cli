package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.RequiredArgsConstructor;
import lombok.val;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class DbStatusOperation {
    private final DbGateway dbGateway;

    public DatabaseStatusType execute(DbRef dbRef) {
        return AstraLogger.loading("Fetching status for database " + highlight(dbRef), (_) -> {
            val db = dbGateway.findOneDb(dbRef);
            return db.getStatus();
        });
    }
}
