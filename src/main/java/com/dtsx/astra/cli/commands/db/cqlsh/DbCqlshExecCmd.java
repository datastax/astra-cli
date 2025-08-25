package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation.ExecSource;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.Optional;

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
public class DbCqlshExecCmd extends DbCqlshStartImpl {
    @Parameters(
        index = "1",
        description = "Execute the statement and quit",
        paramLabel = "STATEMENT",
        arity = "0..1"
    )
    public Optional<String> $statement;

    @Option(
        names = { "-f", "--file" },
        description = "Execute commands from a CQL file, then exit",
        paramLabel = "FILE"
    )
    public Optional<Path> $file;

    @Override
    protected boolean captureOutputForNonHumanOutput() {
        return true;
    }

    @Override
    protected Optional<ExecSource> execSource() {
        // arg group doesn't work well with optional parameters, so we do it manually
        if ($statement.isPresent() && $file.isPresent()) {
            throw new ParameterException(spec.commandLine(), "Cannot specify both a statement and a file to execute; please choose just one.");
        }

        val execSource = $statement.filter(s -> !s.trim().equals("-"))
            .<ExecSource>map(ExecSource.Statement::new)
            .or(() -> $file.map(ExecSource.CqlFile::new))
            .orElseGet(ExecSource.Stdin::new);

        return Optional.of(execSource);
    }
}
