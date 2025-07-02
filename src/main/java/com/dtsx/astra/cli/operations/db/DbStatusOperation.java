package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.RequiredArgsConstructor;
import lombok.val;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class DbStatusOperation implements Operation<DatabaseStatusType> {
    private final DbGateway dbGateway;
    private final DbStatusRequest request;

    public record DbStatusRequest(DbRef dbRef) {}

    @Override
    public DatabaseStatusType execute() {
        return AstraLogger.loading("Fetching status for database " + highlight(request.dbRef), (_) -> {
            val db = dbGateway.findOneDb(request.dbRef);
            return db.getStatus();
        });
    }
}
