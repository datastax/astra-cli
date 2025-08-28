package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkCountOperation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkCountOperation.CountRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "count",
    description = "Count items for a table, a query"
)
public class DbDsbulkCountCmd extends AbstractDsbulkExecWithCoreOptsCmd {
    @Option(
        names = { "-query", "--schema.query" },
        description = "Optional query to unload or count",
        paramLabel = "QUERY"
    )
    public String $query;

    @Override
    protected Operation<DsbulkExecResult> mkOperation() {
        return new DbDsbulkCountOperation(ctx, dbGateway, downloadsGateway, new CountRequest(
            $dbRef,
            $keyspace,
            $table,
            $query,
            $encoding,
            $maxConcurrentQueries,
            $logDir,
            $configProvider(),
            profile().token(),
            $region
        ));
    }
}
