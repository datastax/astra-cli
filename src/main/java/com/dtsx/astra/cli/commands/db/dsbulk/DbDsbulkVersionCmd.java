package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkVersionOperation;
import picocli.CommandLine.Command;

@Command(
    name = "version",
    description = "Display the currently installed dsbulk's version"
)
@Example(
    comment = "Display dsbulk's version information",
    command = "astra db dsbulk version"
)
public class DbDsbulkVersionCmd extends AbstractDsbulkExecCmd {
    @Override
    protected DbDsbulkVersionOperation mkOperation() {
        return new DbDsbulkVersionOperation(dbGateway, downloadsGateway);
    }
}
