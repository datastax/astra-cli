package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.operations.db.DbCqlshOperation;
import com.dtsx.astra.cli.operations.db.DbCqlshOperation.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Optional;

@Command(
    name = "cqlsh",
    description = "Connect to your Astra database using cqlsh"
)
@Example(
    comment = "Launch cqlsh for a database",
    command = "astra db cqlsh my_db"
)
@Example(
    comment = "Launch cqlsh with a specific keyspace",
    command = "astra db cqlsh my_db -k my_keyspace"
)
@Example(
    comment = "Execute a CQL file",
    command = "astra db cqlsh my_db -f script.cql"
)
@Example(
    comment = "Execute a CQL statement",
    command = "astra db cqlsh my_db -e \"SELECT * FROM my_keyspace.my_table\""
)
public class DbCqlshCmd extends AbstractDbSpecificCmd<CqlshResult> {
    @Option(
        names = { "--cqlsh-version" },
        description = "Display cqlsh version information"
    )
    private boolean $version;

    @Option(
        names = { "--debug" },
        description = "Show additional debugging information"
    )
    private boolean $debug;

    @Option(
        names = { "--encoding" },
        description = "Output encoding (default: utf8)",
        paramLabel = "ENCODING"
    )
    private Optional<String> $encoding;

    @Option(
        names = { "-e", "--execute" },
        description = "Execute the statement and quit",
        paramLabel = "STATEMENT"
    )
    private Optional<String> $execute;

    @Option(
        names = { "-f", "--file" },
        description = "Execute commands from a CQL file, then exit",
        paramLabel = "FILE"
    )
    private Optional<File> $file;

    @Option(
        names = { "-k", "--keyspace" },
        description = "Authenticate to the given keyspace",
        paramLabel = "KEYSPACE"
    )
    private Optional<String> $keyspace;

    @Option(
        names = { "--connect-timeout" },
        description = { "Connection timeout in seconds", DEFAULT_VALUE },
        paramLabel = "TIMEOUT",
        defaultValue = "10"
    )
    private int $connectTimeout;

    @Option(
        names = { "--request-timeout" },
        description = { "Request timeout in seconds", DEFAULT_VALUE },
        paramLabel = "TIMEOUT",
        defaultValue = "20"
    )
    private int $requestTimeout;

    @Override
    protected final OutputHuman executeHuman(CqlshResult result) {
        return switch (result) {
            case CqlshInstallFailed(var msg) -> OutputAll.message(msg);
            case ScbDownloadFailed(var msg) -> OutputAll.message(msg);
            case Executed(var exitCode) -> AstraCli.exit(exitCode);
        };
    }

    @Override
    protected DbCqlshOperation mkOperation() {
        return new DbCqlshOperation(dbGateway, downloadsGateway, new CqlshRequest(
            $dbRef,
            $version,
            $debug,
            $encoding,
            $execute,
            $file,
            $keyspace,
            $connectTimeout,
            $requestTimeout,
            profile()
        ));
    }
}
