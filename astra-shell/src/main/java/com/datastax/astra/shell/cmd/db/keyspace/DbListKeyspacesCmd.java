package com.datastax.astra.shell.cmd.db.keyspace;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCmd;
import com.datastax.astra.shell.cmd.db.OperationsDb;
import com.datastax.astra.shell.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.shell.exception.DatabaseNotFoundException;
import com.datastax.astra.shell.exception.ParamValidationException;
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
