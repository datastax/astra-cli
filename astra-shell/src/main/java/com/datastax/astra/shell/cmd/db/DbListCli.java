package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCliCommand;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show Databases for an organization 
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.LIST, description = "Display the list of Databases in an organization")
public class DbListCli extends BaseCliCommand {
   
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationsDb.listDb();
    }

}
