package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.AbstractDbCmd;
import com.dtsx.astra.cli.core.exceptions.internal.misc.WindowsUnsupportedException;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshInstallFailed;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.Executed;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.ScbDownloadFailed;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.function.Supplier;

public abstract class AbstractCqlshExecCmd extends AbstractDbCmd<CqlshExecResult> {
    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        WindowsUnsupportedException.throwIfWindows();
        super.prelude();
    }

    @Override
    protected final OutputHuman executeHuman(Supplier<CqlshExecResult> result) {
        return switch (result.get()) {
            case CqlshInstallFailed(var msg) -> OutputAll.message(msg);
            case ScbDownloadFailed(var msg) -> OutputAll.message(msg);
            case Executed(var exitCode) -> AstraCli.exit(exitCode);
        };
    }
}
