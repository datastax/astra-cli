package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Function;

import static com.dtsx.astra.cli.operations.db.DbGetOperation.DbGetResult;

@RequiredArgsConstructor
public class DbGetOperation implements Operation<DbGetResult> {
    private final DbGateway dbGateway;
    private final DbGetRequest request;

    public record DbGetResult(Database database) {}

    public record DbGetRequest(Optional<DbRef> dbRef, Function<NEList<Database>, Database> promptForDbRef) {}

    @Override
    public DbGetResult execute() {
        val dbRef = request.dbRef.orElseGet(() -> {
            val dbs = dbGateway.findAll().toList();

            val chosenDb = NEList.parse(dbs)
               .map(request.promptForDbRef)
               .orElseThrow(() -> new IllegalArgumentException("No databases found in the organization"));

            return DbRef.fromNameUnsafe(chosenDb.getInfo().getName());
        });

        return new DbGetResult(dbGateway.findOne(dbRef));
    }
}
