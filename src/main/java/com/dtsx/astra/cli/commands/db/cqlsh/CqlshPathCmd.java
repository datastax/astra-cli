package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.commands.db.AbstractDbCmd;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshPathOperation;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshPathOperation.CqlPathResponse;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshPathOperation.ExePathFound;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshPathOperation.InstallationFailed;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshPathOperation.NoInstallationFound;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.commands.db.cqlsh.AbstractCqlshExecCmd.throwCqlshInstallationFailed;
import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;

@Command(
    name = "path",
    description = "Get the path to the currently installed cqlsh executable"
)
@Example(
    comment = "Get the path to the cqlsh executable",
    command = "${cli.name} db cqlsh path"
)
@Example(
    comment = "Use the cqlsh exe to run a custom command",
    command = "$(${cli.name} db cqlsh path) --help"
)
@Example(
    comment = "Get path only if cqlsh exists, don't install",
    command = "${cli.name} db cqlsh path --if-exists"
)
public class CqlshPathCmd extends AbstractDbCmd<CqlPathResponse> {
    @Option(
        names = { "--if-exists" },
        description = "Only return path if cqlsh exists, don't install automatically"
    )
    public boolean $ifExists;

    @Override
    protected Operation<CqlPathResponse> mkOperation() {
        return new DbCqlshPathOperation(ctx, ctx.gateways().mkDownloadsGateway(), !$ifExists);
    }

    @Override
    protected final OutputAll execute(Supplier<CqlPathResponse> result) {
        return switch (result.get()) {
            case ExePathFound(var file) -> handleExePathFound(file);
            case NoInstallationFound _ -> throwNoInstallationFound();
            case InstallationFailed(var error) -> throwCqlshInstallationFailed(error);
        };
    }

    public OutputAll handleExePathFound(Path file) {
        return OutputAll.response(file.toString());
    }

    public <T> T throwNoInstallationFound() {
        throw new AstraCliException(FILE_ISSUE, """
          @|bold,red No cqlsh installation was found in '%s'.|@
        
          Please install @!cqlsh!@ by running any cqlsh command through @!${cli.name}!@.
        
          @|faint,italic Note that the CLI does not recognize cqlsh installations done outside of astra.|@
        """.formatted(ctx.home().root()), List.of(
            new Hint("Example command to install cqlsh (no --if-exists flag):", "${cli.name} db cqlsh path")
        ));
    }
}
