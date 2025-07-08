package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.AbstractDbCmd;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshInstallFailed;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.Executed;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.ScbDownloadFailed;

public abstract class AbstractCqlshExecCmd extends AbstractDbCmd<CqlshExecResult> {
    @Override
    protected final OutputHuman executeHuman(CqlshExecResult result) {
        return switch (result) {
            case CqlshInstallFailed(var msg) -> OutputAll.message(msg);
            case ScbDownloadFailed(var msg) -> OutputAll.message(msg);
            case Executed(var exitCode) -> AstraCli.exit(exitCode);
        };
    }
}
