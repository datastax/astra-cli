package com.datastax.astra.cli.db;

import java.util.Optional;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.AbstractInteractiveCmd;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.sdk.databases.DatabaseClient;
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
        Optional<DatabaseClient> dbClient = OperationsDb.getDatabaseClient(database);
        if (dbClient.isPresent()) {
            ctx().useDatabase(dbClient.get().find().get());
            LoggerShell.info("Selecting Database '" + database + "'");
        } else {
            throw new DatabaseNotFoundException(database);
        }
    } 
    
}
