package com.datastax.astra.cli.db;

import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.exception.InvalidDatabaseStateException;
import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

import java.util.logging.Logger;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "delete", description = "Delete an existing database")
public class DbDeleteCmd extends AbstractDatabaseCmd {

    /**
     * Will wait until the database become ACTIVE.
     */
    @Option(name = { "--wait" },
            description = "Will wait until the database become ACTIVE")
    protected boolean wait = false;

    /**
     * Provide a limit to the wait period in seconds, default is 180s.
     */
    @Option(name = { "--timeout" },
            description = "Provide a limit to the wait period in seconds, default is 300s.")
    protected int timeout = 300;

    /** {@inheritDoc} */
    public void execute() {
        dbServices.deleteDb(db);
        if (wait) {
            if (dbServices.retryUntilDbDeleted(db, timeout) >= timeout) {
                throw new InvalidDatabaseStateException(db, DatabaseStatusType.TERMINATED,
                        DatabaseStatusType.TERMINATING);
            } else {
                AstraCliConsole.outputSuccess("Database %s has been deleted".formatted(db));
            }
        }
    }
    
}
