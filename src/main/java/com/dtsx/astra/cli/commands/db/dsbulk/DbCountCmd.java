package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkResult;
import picocli.CommandLine.Command;

@Command(
    name = "count"
)
public class DbCountCmd extends AbstractDbDsbulkExecCmd {
    @Override
    protected Operation<DsbulkResult> mkOperation() {
        return null;
    }
}
