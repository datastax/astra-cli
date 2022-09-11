package com.datastax.astra.cli.db;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.BaseCmd;
import com.datastax.astra.cli.core.exception.ParamValidationException;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsDb.CMD_STATUS, description = "Show details of a database")
public class DbStatusCmd extends BaseCmd {

    /** name of the DB. */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String database;
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        return OperationsDb.showDbStatus(database);
    }

}
