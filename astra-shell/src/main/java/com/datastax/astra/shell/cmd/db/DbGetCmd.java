package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCmd;
import com.datastax.astra.shell.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.shell.exception.DatabaseNotFoundException;
import com.datastax.astra.shell.exception.ParamValidationException;
import com.datastax.astra.shell.cmd.AbstractCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.GET, description = "Show details of a database")
public class DbGetCmd extends BaseCmd {

    /** name of the DB. */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String database;
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {    
        return OperationsDb.showDb(database);
    }

}
