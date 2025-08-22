package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshExecOperation;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshExecOperation.ExecRequest;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "exec",
    description = "Execute CQL statements for your db using cqlsh"
)
@Example(
    comment = "Execute a CQL statement",
    command = "${cli.name} db cqlsh exec my_db \"SELECT * FROM my_keyspace.my_table\""
)
@Example(
    comment = "Execute a CQL file",
    command = "${cli.name} db cqlsh exec my_db -f script.cql"
)
public class DbCqlshExecCmd extends DbCqlshStartCmd {
    @Parameters(
        index = "1",
        description = "Execute the statement and quit",
        paramLabel = "STATEMENT"
    )
    public Optional<String> $statement;

    @Option(
        names = { "-f", "--file" },
        description = "Execute commands from a CQL file, then exit",
        paramLabel = "FILE"
    )
    public Optional<File> $file;

    @Override
    protected Operation<CqlshExecResult> mkOperation() {
        val execSource = $statement.filter(s -> !s.trim().equals("-")).<Either<String, File>>map(Either::left)
            .or(() -> $file.map(Either::right));

        if ($statement.isPresent() && $file.isPresent()) {
            throw new ParameterException(spec.commandLine(), "Cannot specify both a statement and a file to execute; please choose just one.");
        }

        return new DbCqlshExecOperation(dbGateway, downloadsGateway, new ExecRequest(
            $dbRef,
            $debug,
            $encoding,
            execSource,
            $keyspace,
            $connectTimeout,
            $requestTimeout,
            profile(),
            this::readStdin
        ));
    }

    private String readStdin() {
        try (val reader = new BufferedReader(new InputStreamReader(AstraConsole.getIn()))) {
            val sb = new StringBuilder();
            var line = "";

            while ((line = reader.readLine()) != null) {
                if (line.trim().endsWith("\\")) {
                    sb.append(line, 0, line.lastIndexOf('\\') - 1);
                    sb.append(NL);
                } else {
                    sb.append(line);
                    break;
                }
            }

            return sb.toString();
        } catch (IOException e) {
            AstraLogger.exception(e);

            throw new AstraCliException("""
              @|bold,red Error: Attempted to read from standard input, but something went wrong:|@
            
              "%s"
            """.formatted(e.getMessage()));
        }
    }
}
