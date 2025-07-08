package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import picocli.CommandLine.Command;

@Command(
    name = "count"
)
public class DbCountCmd extends AbstractDsbulkExecCmd {
    @Override
    protected Operation<DsbulkExecResult> mkOperation() {
        return null;
    }
}
