package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;
import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCmd;
import com.datastax.astra.shell.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.shell.exception.DatabaseNotFoundException;
import com.datastax.astra.shell.exception.ParamValidationException;
import com.datastax.astra.shell.out.LoggerShell;
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
public class DbResumeCmd extends BaseCmd {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String databaseName;
    
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
    
    /** {@inheritDoc} 
     * @throws DatabaseNameNotUniqueException */
    public ExitCode execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        ExitCode code = OperationsDb.resumeDb(databaseName);
        if (ExitCode.SUCCESS.equals(code)) {
            LoggerShell.success("Database \'" + databaseName +  "' is resuming");
        }
        if (ExitCode.SUCCESS.equals(code) && wait) {
            code = OperationsDb.waitForDbStatus(databaseName, DatabaseStatusType.ACTIVE, timeout);
        }
        return code;
    }
    
}
