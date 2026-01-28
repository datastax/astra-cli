package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation.ExecSource;
import lombok.val;
import picocli.CommandLine.*;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@Command(
    name = "exec",
    description = "Execute CQL statements for your db using cqlsh"
)
@Example(
    comment = "Execute a CQL statement",
    command = "${cli.name} db cqlsh exec my_db -e \"SELECT * FROM my_keyspace.my_table\""
)
@Example(
    comment = "Execute a CQL file",
    command = "${cli.name} db cqlsh exec my_db -f script.cql"
)
public class CqlshExecCmd extends CqlshStartImpl {
    @Parameters(
        index = "1",
        description = "Execute the statement and quit",
        paramLabel = "STATEMENT",
        arity = "0..1",
        hidden = true
    )
    public Optional<String> $legacyStatement; // this was a mistake because it doesn't work well w/ optional first param

    @ArgGroup
    public RawExecSource $execSource;

    public static class RawExecSource {
        @Option(
            names = { "-e", "--execute" },
            description = "Execute the statement then exit",
            fallbackValue = "-",
            arity = "0..1",
            paramLabel = "STATEMENT"
        )
        public Optional<String> $statement = Optional.empty();

        @Option(
            names = { "-f", "--file" },
            description = "Execute commands from a CQL file, then exit",
            paramLabel = "FILE"
        )
        public Optional<Path> $file = Optional.empty();
    }

    @Override
    protected boolean captureOutputForNonHumanOutput() {
        return true;
    }

    @Override
    protected Optional<ExecSource> execSource() {
        if ($legacyStatement.isPresent()) {
            ctx.log().warn("Providing the CQL statement as a positional parameter is @!deprecated!@ and will be removed in future versions.");
            ctx.log().warn("Please use the @'!${cli.name} db cqlsh exec -e \"CQL STATEMENT\"!@ syntax instead.");
        }

        val source = Objects.requireNonNullElseGet($execSource, RawExecSource::new);

        if ($legacyStatement.isPresent() && source.$statement.isPresent()) {
            throw new ParameterException(spec.commandLine(), "Cannot specify the CQL statement both as a positional parameter and with the -e/--execute option; please use the -e flag from now on, as positional statements are deprecated.");
        }

        val statement = (source.$statement.isPresent())
            ? $execSource.$statement
            : $legacyStatement;

        if (statement.isPresent() && source.$file.isPresent()) {
            throw new ParameterException(spec.commandLine(), "Cannot specify both a statement and a file to execute; please choose just one.");
        }

        val execSource = statement.filter(s -> !s.trim().equals("-"))
            .<ExecSource>map(ExecSource.Statement::new)
            .or(() -> source.$file.map(ExecSource.CqlFile::new))
            .orElseGet(ExecSource.Stdin::new);

        return Optional.of(execSource);
    }
}
