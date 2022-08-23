package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCliCommand;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
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
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationsDb.resumeDb(databaseId);
    }
    
}
