package com.datastax.astra.cli.cmd.db;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.cmd.AbstractCmd;
import com.datastax.astra.cli.cmd.BaseCmd;
import com.datastax.astra.cli.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.exception.ParamValidationException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.DELETE, description = "Delete an existing database")
public class DbDeleteCmd extends BaseCmd {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String databaseId;
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        return OperationsDb.deleteDb(databaseId);
    }
    
}
