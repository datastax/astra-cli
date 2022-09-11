package com.datastax.astra.cli.db.keyspace;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.BaseCmd;
import com.datastax.astra.cli.core.exception.ParamValidationException;
import com.datastax.astra.cli.db.OperationsDb;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Show Keyspaces for an Database.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsDb.CMD_LIST_KEYSPACES, description = "Display the list of Keyspaces in an database")
public class DbListKeyspacesCmd extends BaseCmd {
   
    /** name of the DB. */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String database;
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        return OperationsDb.listKeyspaces(database);
    }

}
