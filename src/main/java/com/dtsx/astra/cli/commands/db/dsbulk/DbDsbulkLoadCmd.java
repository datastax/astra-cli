package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkLoadOperation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkLoadOperation.LoadRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;

@Command(
    name = "load",
    description = "Load data leveraging DSBulk"
)
@Example(
    comment = "Load a CSV that includes a header row",
    command = {
        "astra db dsbulk load my_db -k my_keyspace -t customers",
        "--url dsbulk_samples/customers.csv",
        "--header",
    }
)
@Example(
    comment = "Load a delimited file with no header",
    command = {
        "astra db dsbulk load my_db -k my_keyspace -t products",
        "--url dsbulk_samples/products.psv",
        "--delimiter '|'",
        "-m '0=sku,1=name,2=category,3=price,4=inventory'",
    }
)
@Example(
    comment = "Map input field names to different table columns",
    command = {
        "astra db dsbulk load my_db -k my_keyspace -t orders_by_customer",
        "--url dsbulk_samples/orders_legacy.csv",
        "--header",
        "-m 'cust_id=customer_id,ts=order_ts,id=order_id,status=status,total_usd=total,item_count=items'",
        "--log-dir dsbulk_out/logs/load-orders",
    }
)
@Example(
    comment = "Skip preamble lines and use a dsbulk config file",
    command = {
        "astra db dsbulk load my_db -k my_keyspace -t orders_by_customer",
        "--url dsbulk_samples/orders_export.csv",
        "--skip-records 1",
        "--header",
        "--dsbulk-config dsbulk_samples/dsbulk-timestamp.conf",
        "--max-errors 25",
    }
)
public class DbDsbulkLoadCmd extends AbstractDsbulkExecWithCoreOptsCmd {
    @Option(
        names = { "--url" },
        description = "File location to load data",
        paramLabel = "URL",
        required = true
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
        description = "Read, Write Header in input file",
        defaultValue = "true",
        fallbackValue = "true"
    )
    public boolean $header;

    @Option(
        names = { "--skip-records" },
        description = "Lines to skip before reading",
        paramLabel = "COUNT",
        defaultValue = "0"
    )
    public int $skipRecords;

    @Option(
        names = { "--max-errors" },
        description = "Maximum number of errors before aborting the operation",
        paramLabel = "COUNT",
        defaultValue = "100"
    )
    public int $maxErrors;

    @Option(
        names = { "--dry-run" },
        description = "Enable or disable dry-run mode"
    )
    public boolean $dryRun;

    @Option(
        names = { "--allow-missing-fields" },
        description = "Ease the mapping"
    )
    public boolean $allowMissingFields;

    @Override
    protected Operation<DsbulkExecResult> mkOperation() {
        return new DbDsbulkLoadOperation(ctx, dbGateway, downloadsGateway, new LoadRequest(
            $dbRef,
            $keyspace,
            $table,
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
            $dryRun,
            $allowMissingFields,
            $region
        ));
    }
}
