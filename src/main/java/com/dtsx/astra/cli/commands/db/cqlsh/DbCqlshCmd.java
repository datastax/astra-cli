package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshOperation;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshOperation.CqlshRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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
public class DbCqlshCmd extends AbstractDbCqlshExecCmd {
    @Parameters(
        index = "0",
        completionCandidates = DbNamesCompletion.class,
        paramLabel = "DB",
        description = "The name/ID of the Astra database to connect to"
    )
    public DbRef $dbRef;

    @Option(
        names = { "--cqlsh-version" },
        description = "Display cqlsh version information"
    )
    public boolean $version;

    @Option(
        names = { "--debug" },
        description = "Show additional debugging information"
    )
    public boolean $debug;

    @Option(
        names = { "--encoding" },
        description = "Output encoding (default: utf8)",
        paramLabel = "ENCODING"
    )
    public Optional<String> $encoding;

    @Option(
        names = { "-e", "--execute" },
        description = "Execute the statement and quit",
        paramLabel = "STATEMENT"
    )
    public Optional<String> $execute;

    @Option(
        names = { "-f", "--file" },
        description = "Execute commands from a CQL file, then exit",
        paramLabel = "FILE"
    )
    public Optional<File> $file;

    @Option(
        names = { "-k", "--keyspace" },
        description = "Authenticate to the given keyspace",
        paramLabel = "KEYSPACE"
    )
    public Optional<String> $keyspace;

    @Option(
        names = { "--connect-timeout" },
        description = { "Connection timeout in seconds", DEFAULT_VALUE },
        paramLabel = "TIMEOUT",
        defaultValue = "10"
    )
    public int $connectTimeout;

    @Option(
        names = { "--request-timeout" },
        description = { "Request timeout in seconds", DEFAULT_VALUE },
        paramLabel = "TIMEOUT",
        defaultValue = "20"
    )
    public int $requestTimeout;

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
