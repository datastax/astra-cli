package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.AbstractDbCmd;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.misc.WindowsUnsupportedException;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkInstallFailed;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.Executed;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.ScbDownloadFailed;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;

public abstract class AbstractDsbulkExecCmd extends AbstractDbCmd<DsbulkExecResult> {
    protected DownloadsGateway downloadsGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        WindowsUnsupportedException.throwIfWindows(ctx);
        super.prelude();
        downloadsGateway = ctx.gateways().mkDownloadsGateway(ctx);
    }

    @Override
    protected final OutputHuman executeHuman(Supplier<DsbulkExecResult> result) {
        return switch (result.get()) {
            case DsbulkInstallFailed(var msg) -> throwDsbulkInstallationFailed(msg);
            case ScbDownloadFailed(var msg) -> throwDsbulkInstallationFailed(msg);
            case Executed(var exitCode) -> AstraCli.exit(exitCode);
        };
    }

    public static <T> T throwDsbulkInstallationFailed(String error) {
        throw new AstraCliException(FILE_ISSUE, """
          @|bold,red Failed to install dsbulk: %s|@
        
          Please ensure you have a stable network connection and sufficient permissions, then try again.
        """.formatted(error), List.of(
            new Hint("Retry installation:", "${cli.name} db dsbulk version")
        ));
    }
}
