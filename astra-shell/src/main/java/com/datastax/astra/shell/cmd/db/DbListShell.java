package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.datastax.astra.shell.cmd.BaseShellCommand;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show Databases for an organization 
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.LIST, description = "Display the list of Databases in an organization")
public class DbListShell extends BaseShellCommand {
   
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationsDb.listDb();
    }

}
