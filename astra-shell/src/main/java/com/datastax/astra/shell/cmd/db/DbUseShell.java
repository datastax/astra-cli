package com.datastax.astra.shell.cmd.db;

import java.util.Optional;

import com.datastax.astra.sdk.databases.DatabaseClient;
import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.datastax.astra.shell.cmd.BaseShellCommand;
import com.datastax.astra.shell.out.LoggerShell;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Select one Database in the Shell.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.USE, description = "Select a database in the shell")
public class DbUseShell extends BaseShellCommand {

    /** Database name of identifier. */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String database;

    /** {@inheritDoc} */
    @Override
    public ExitCode execute() {
        // We want the use command to be verbose
        this.verbose = true;
        Optional<DatabaseClient> dbClient = OperationsDb.getDatabaseClient(database);
        if (dbClient.isPresent()) {
            ctx().useDatabase(dbClient.get().find().get());
            LoggerShell.info("Selecting Database '" + database + "'");
            return ExitCode.SUCCESS;
        }
        return ExitCode.NOT_FOUND;
    } 
    
}
