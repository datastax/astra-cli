package com.datastax.astra.cli.db;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.BaseCmd;
import com.datastax.astra.cli.core.exception.ParamValidationException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;
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
    
    /** {@inheritDoc} */
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
