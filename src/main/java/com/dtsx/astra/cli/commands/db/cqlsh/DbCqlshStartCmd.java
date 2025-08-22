package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation.CqlshRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Optional;

@Command(
    name = "start",
    description = "Connect to your Astra database using cqlsh"
)
@Example(
    comment = "Launch cqlsh for a database",
    command = "astra db cqlsh start my_db"
)
@Example(
    comment = "Launch cqlsh with a specific keyspace",
    command = "astra db cqlsh start my_db -k my_keyspace"
)
public class DbCqlshStartCmd extends AbstractCqlshExecCmd {
    @Parameters(
        index = "0",
        completionCandidates = DbNamesCompletion.class,
        paramLabel = "DB",
        description = "The name/ID of the Astra database to connect to"
    )
    public DbRef $dbRef;

//    @Option(
//        names = { "-e", "--execute" },
//        description = "Execute the statement and quit",
//        paramLabel = "STATEMENT"
//    )
//    public Optional<String> $execute;
//
//    @Option(
//        names = { "-f", "--file" },
//        description = "Execute commands from a CQL file, then exit",
//        paramLabel = "FILE"
//    )
//    public Optional<File> $file;

    @Option(
        names = { "-k", "--keyspace" },
        description = "Authenticate to the given keyspace",
        paramLabel = "KEYSPACE"
    )
    public Optional<String> $keyspace;

    @Option(
        names = { "--request-timeout" },
        description = { "Request timeout in seconds", DEFAULT_VALUE },
        paramLabel = "TIMEOUT",
        defaultValue = "20"
    )
    public int $requestTimeout;

    @Override
    protected Operation<CqlshExecResult> mkOperation() {
        return new DbCqlshStartOperation(dbGateway, downloadsGateway, new CqlshRequest(
            $dbRef,
            $debug,
            $encoding,
            $keyspace,
            $connectTimeout,
            $requestTimeout,
            profile()
        ));
    }
}
