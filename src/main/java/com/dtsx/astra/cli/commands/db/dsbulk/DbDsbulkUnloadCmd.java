package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkUnloadOperation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkUnloadOperation.UnloadRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;

@Command(
    name = "unload",
    description = "Unload data leveraging DSBulk"
)
@Example(
    comment = "Unload a table to a local directory",
    command = {
        "astra db dsbulk unload my_db -k my_keyspace -t customers",
        "--url dsbulk_out/customers",
        "--header",
    }
)
@Example(
    comment = "Unload selected columns with renamed output headers",
    command = {
        "astra db dsbulk unload my_db -k my_keyspace -t products",
        "--url dsbulk_out/products_pipe",
        "--delimiter '|'",
        "-m 'product_sku=sku,product_name=name,usd_price=price'",
        "--header",
    }
)
@Example(
    comment = "Unload only rows that match a query",
    command = {
        "astra db dsbulk unload my_db -k my_keyspace",
        "-query \"SELECT * FROM orders_by_customer WHERE customer_id = 111111-1111-1111-1111-111111111111\"",
        "--url dsbulk_out/orders_one_customer",
        "--header",
    }
)
@Example(
    comment = "Unload a large table with custom output file naming and tuned paging",
    command = {
        "astra db dsbulk unload my_db -k my_keyspace -t orders_by_customer",
        "--url dsbulk_out/orders",
        "-F '--connector.csv.fileNameFormat=orders-%06d.csv'",
        "-F '--driver.basic.request.page-size=5000'",
        "--max-concurrent-queries 32",
        "--header",
    }
)
public class DbDsbulkUnloadCmd extends AbstractDsbulkExecWithCoreOptsCmd {
    @Option(
        names = { "--url" },
        description = "The URL or path of the resource(s) to read from or write to",
        paramLabel = "URL",
        defaultValue = "./data"
    )
    public String $url;

    @Option(
        names = { "--delimiter" },
        description = "Character(s) use as field delimiter",
        paramLabel = "DELIMITER",
        defaultValue = ","
    )
    public String $delimiter;

    @Option(
        names = { "-m", "--schema.mapping" },
        description = "Field-to-column mapping to use",
        paramLabel = "MAPPING"
    )
    public Optional<String> $mapping;

    @Option(
        names = { "--header" },
        description = "Enable or disable whether the files begin with a header line",
        defaultValue = "true",
        fallbackValue = "true"
    )
    public boolean $header;

    @Option(
        names = { "--skip-records" },
        description = "Number of records to skip from each input file before parsing",
        paramLabel = "COUNT",
        defaultValue = "0"
    )
    public int $skipRecords;

    @Option(
        names = { "--max-errors" },
        description = "Maximum number of errors to tolerate before aborting the operation",
        paramLabel = "COUNT",
        defaultValue = "100"
    )
    public int $maxErrors;

    @Option(
        names = { "-query", "--schema.query" },
        description = "Optional query to unload or count",
        paramLabel = "QUERY"
    )
    public Optional<String> $query;

    @Override
    protected Operation<DsbulkExecResult> mkOperation() {
        return new DbDsbulkUnloadOperation(ctx, dbGateway, downloadsGateway, new UnloadRequest(
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
            $url,
            $delimiter,
            $mapping,
            $header,
            $skipRecords,
            $maxErrors,
            $region
        ));
    }
}
