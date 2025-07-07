package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkInstallFailed;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkResult;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.Executed;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.ScbDownloadFailed;

public abstract class AbstractDbDsbulkExecCmd extends AbstractDbSpecificCmd<DsbulkResult> {
    @Override
    protected final OutputHuman executeHuman(DsbulkResult result) {
        return switch (result) {
            case DsbulkInstallFailed(var msg) -> OutputAll.message(msg);
            case ScbDownloadFailed(var msg) -> OutputAll.message(msg);
            case Executed(var exitCode) -> AstraCli.exit(exitCode);
        };
    }
}
