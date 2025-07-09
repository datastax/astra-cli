package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import com.dtsx.astra.cli.operations.db.dsbulk.DbCountOperation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbCountOperation.CountRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "count",
    description = "Count items for a table, a query"
)
public class DbCountCmd extends AbstractDsbulkExecCmd {
    @Option(
        names = { "-query", "--schema.query" },
        description = "Optional query to unload or count",
        paramLabel = "QUERY"
    )
    public String $query;

    @Override
    protected Operation<DsbulkExecResult> mkOperation() {
        return new DbCountOperation(dbGateway, downloadsGateway, new CountRequest(
            $dbRef,
            $keyspace,
            $table,
            $query,
            $encoding,
            $maxConcurrentQueries,
            $logDir,
            $configProvider(),
            profile().token()
        ));
    }
}
