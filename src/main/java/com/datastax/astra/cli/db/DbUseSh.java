package com.datastax.astra.cli.db;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.AbstractInteractiveCmd;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Select one Database in the Shell.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.USE, description = "Select a database in the shell")
public class DbUseSh extends AbstractInteractiveCmd {

    /** Database name of identifier. */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String database;

    /** {@inheritDoc} */
    @Override
    public void execute() throws Exception {
        this.verbose = true;
        ctx().useDatabase(OperationsDb.getDatabase(database));
        LoggerShell.info("Selecting Database '" + database + "'");
    } 
    
}
