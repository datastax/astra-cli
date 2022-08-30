package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;
import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCliCommand;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsDb.CMD_RESUME, description = "Resume a db if needed")
public class DbResumeCli extends BaseCliCommand {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String databaseId;
    
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
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        ExitCode code = OperationsDb.resumeDb(databaseId);
        // Creation request is a success but waiting for proper status
        if (ExitCode.SUCCESS.equals(code) && wait) {
            return OperationsDb.waitForDbStatus(databaseId, DatabaseStatusType.ACTIVE, timeout);
        }
        return code;
    }
    
}
