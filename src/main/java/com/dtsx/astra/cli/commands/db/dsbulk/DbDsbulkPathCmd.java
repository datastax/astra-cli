package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.commands.db.AbstractDbCmd;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkPathOperation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkPathOperation.DsbulkPathResponse;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkPathOperation.ExePathFound;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkPathOperation.InstallationFailed;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkPathOperation.NoInstallationFound;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.commands.db.dsbulk.AbstractDsbulkExecCmd.throwDsbulkInstallationFailed;
import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;

@Command(
    name = "path",
    description = "Get the path to the currently installed dsbulk executable"
)
@Example(
    comment = "Get the path to the dsbulk executable",
    command = "${cli.name} db dsbulk path"
)
@Example(
    comment = "Use the dsbulk exe to run a custom command",
    command = "$(${cli.name} db dsbulk path) --help"
)
@Example(
    comment = "Get path only if dsbulk exists, don't install",
    command = "${cli.name} db dsbulk path --if-exists"
)
public class DbDsbulkPathCmd extends AbstractDbCmd<DsbulkPathResponse> {
    @Option(
        names = { "--if-exists" },
        description = "Only return path if dsbulk exists, don't install automatically"
    )
    public boolean $ifExists;

    @Override
    protected Operation<DsbulkPathResponse> mkOperation() {
        return new DbDsbulkPathOperation(ctx, ctx.gateways().mkDownloadsGateway(ctx), !$ifExists);
    }

    @Override
    protected final OutputAll execute(Supplier<DsbulkPathResponse> result) {
        return switch (result.get()) {
            case ExePathFound(var file) -> handleExePathFound(file);
            case NoInstallationFound _ -> throwNoInstallationFound();
            case InstallationFailed(var error) -> throwDsbulkInstallationFailed(error);
        };
    }

    public OutputAll handleExePathFound(Path file) {
        return OutputAll.response(file.toString());
    }

    public <T> T throwNoInstallationFound() {
        throw new AstraCliException(FILE_ISSUE, """
          @|bold,red No dsbulk installation was found in '%s'.|@
        
          Please install @!dsbulk!@ by running any dsbulk command through @!${cli.name}!@.
        
          @|faint,italic Note that the CLI does not recognize dsbulk installations done outside of astra.|@
        """.formatted(ctx.home().getDir()), List.of(
            new Hint("Example command to install dsbulk (no --if-exists flag):", "${cli.name} db dsbulk path")
        ));
    }
}
