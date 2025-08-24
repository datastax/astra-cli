package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkUnloadOperation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkUnloadOperation.UnloadRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "unload",
    description = "Unload data leveraging DSBulk"
)
public class DbDsbulkUnloadCmd extends AbstractDsbulkExecWithCoreOptsCmd {
    @Option(
        names = { "--url" },
        description = { "The URL or path of the resource(s) to read from or write to", DEFAULT_VALUE },
        paramLabel = "URL",
        defaultValue = "./data"
    )
    public String $url;

    @Option(
        names = { "--delimiter" },
        description = { "Character(s) use as field delimiter", DEFAULT_VALUE },
        paramLabel = "DELIMITER",
        defaultValue = ","
    )
    public String $delimiter;

    @Option(
        names = { "-m", "--schema.mapping" },
        description = "Field-to-column mapping to use",
        paramLabel = "MAPPING"
    )
    public String $mapping;

    @Option(
        names = { "--header" },
        description = { "Enable or disable whether the files begin with a header line", DEFAULT_VALUE },
        defaultValue = "true"
    )
    public boolean $header;

    @Option(
        names = { "--skip-records" },
        description = { "Number of records to skip from each input file before parsing", DEFAULT_VALUE },
        paramLabel = "COUNT",
        defaultValue = "0"
    )
    public int $skipRecords;

    @Option(
        names = { "--max-errors" },
        description = { "Maximum number of errors to tolerate before aborting the operation", DEFAULT_VALUE },
        paramLabel = "COUNT",
        defaultValue = "100"
    )
    public int $maxErrors;
    @Option(
        names = { "-query", "--schema.query" },
        description = "Optional query to unload or count",
        paramLabel = "QUERY"
    )
    public String $query;

    @Override
    protected Operation<DsbulkExecResult> mkOperation() {
        return new DbDsbulkUnloadOperation(dbGateway, downloadsGateway, new UnloadRequest(
            $dbRef,
            $keyspace,
            $table,
            $query,
            $encoding,
            $maxConcurrentQueries,
            $logDir,
            $configProvider(),
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
