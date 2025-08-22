package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkLoadOperation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkLoadOperation.LoadRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "load",
    description = "Load data leveraging DSBulk"
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
        description = { "Read, Write Header in input file", DEFAULT_VALUE },
        defaultValue = "true"
    )
    public boolean $header;

    @Option(
        names = { "--skip-records" },
        description = { "Lines to skip before reading", DEFAULT_VALUE },
        paramLabel = "COUNT",
        defaultValue = "0"
    )
    public int $skipRecords;

    @Option(
        names = { "--max-errors" },
        description = { "Maximum number of errors before aborting the operation", DEFAULT_VALUE },
        paramLabel = "COUNT",
        defaultValue = "100"
    )
    public int $maxErrors;

    @Option(
        names = { "--dry-run" },
        description = { "Enable or disable dry-run mode", DEFAULT_VALUE }
    )
    public boolean $dryRun;

    @Option(
        names = { "--allow-missing-fields" },
        description = { "Ease the mapping", DEFAULT_VALUE }
    )
    public boolean $allowMissingFields;

    @Override
    protected Operation<DsbulkExecResult> mkOperation() {
        return new DbDsbulkLoadOperation(dbGateway, downloadsGateway, new LoadRequest(
            $dbRef,
            $keyspace,
            $table,
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
            $dryRun,
            $allowMissingFields
        ));
    }
}
