package com.datastax.astra.cli.db;

import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.InvalidDatabaseStateException;
import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "resume", description = "Resume a db if needed")
public class DbResumeCmd extends AbstractDatabaseCmd {
    
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
            description = "Provide a limit to the wait period in seconds, default is 180s.")
    protected int timeout = 180;
    
    /** {@inheritDoc}  */
    public void execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, 
           InvalidDatabaseStateException  {
        dbServices.resumeDb(db);
        if (wait) {
           switch(dbServices.waitForDbStatus(db, DatabaseStatusType.ACTIVE, timeout)) {
            case NOT_FOUND:
                throw new DatabaseNotFoundException(db);
            case UNAVAILABLE:
                throw new InvalidDatabaseStateException(db, DatabaseStatusType.ACTIVE,  DatabaseStatusType.HIBERNATED);
            default:
                LoggerShell.success("Database \'%s' has resumed".formatted(db));
            break;
           }
        }
    }
    
}
