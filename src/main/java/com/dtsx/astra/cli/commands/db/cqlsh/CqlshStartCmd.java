package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation.ExecSource;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Optional;


@Command(
    name = "start",
    description = "Connect to your Astra database using cqlsh"
)
@Example(
    comment = "Launch cqlsh for a database",
    command = "${cli.name} db cqlsh start my_db"
)
@Example(
    comment = "Launch cqlsh with a specific keyspace",
    command = "${cli.name} db cqlsh start my_db -k my_keyspace"
)
public class CqlshStartCmd extends CqlshStartImpl {
    @ArgGroup
    public @Nullable RawExecSource $execSource;

    public static class RawExecSource {
        @Option(
            names = { "-e", "--execute" },
            description = "Execute the statement then quit",
            paramLabel = "STATEMENT",
            hidden = true
        )
        public Optional<String> $statement;

        @Option(
            names = { "-f", "--file" },
            description = "Execute commands from a CQL file then quit",
            paramLabel = "FILE",
            hidden = true
        )
        public Optional<Path> $file;
    }

    @Override
    protected boolean captureOutputForNonHumanOutput() {
        return false;
    }

    @Override
    protected Optional<ExecSource> execSource() {
        val ret = Optional.ofNullable($execSource)
            .flatMap(raw -> raw.$statement
                .<ExecSource>map(ExecSource.Statement::new)
                .or(() -> raw.$file.map(ExecSource.CqlFile::new))
            );

        if (ret.isPresent()) {
            ctx.log().warn("The @'!---execute!@ and @'!--file!@ options are @!deprecated!@ and will be removed in future versions.");
            ctx.log().warn("Please use the " + ctx.highlight("${cli.name} db cqlsh exec") + " command instead.");
        }

        return ret;
    }
}
