package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkCountOperation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkCountOperation.CountRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;

@Command(
    name = "count",
    description = "Count items for a table, a query"
)
@Example(
    comment = "Count all rows in a table",
    command = "astra db dsbulk count my_db -k my_keyspace -t users"
)
@Example(
    comment = "Count rows using a custom query with filtering",
    command = {
        "astra db dsbulk count my_db -k my_keyspace",
        "-query \"SELECT * FROM orders_by_customer WHERE customer_id = 11111111-1111-1111-1111-111111111111\"",
    }
)
@Example(
    comment = "Count with increased parallelism for large tables",
    command = {
        "astra db dsbulk count my_db -k my_keyspace -t products",
        "--max-concurrent-queries 32"
    }
)
@Example(
    comment = "Find the largest partitions using dsbulk stats flags",
    command = {
        "astra db dsbulk count my_db -k my_keyspace -t orders_by_customer",
        "-F '--stats.modes=partitions'",
        "-F '--stats.numPartitions=5'",
    }
)
public class DbDsbulkCountCmd extends AbstractDsbulkExecWithCoreOptsCmd {
    @Option(
        names = { "-query", "--schema.query" },
        description = "Optional query to unload or count",
        paramLabel = "QUERY"
    )
    public Optional<String> $query;

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
            $configFile,
            $flags(),
            profile().token(),
            $region
        ));
    }
}
