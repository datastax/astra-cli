package com.datastax.astra.cli.db.keyspace;

import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.db.AbstractDatabaseCmd;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show Keyspaces for an Database.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "list-keyspaces", description = "Display the list of Keyspaces in an database")
public class DbListKeyspacesCmd extends AbstractDatabaseCmd {
    
    /** {@inheritDoc} */
    public void execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException {
        dbServices.listKeyspaces(db);
    }

}
