package com.datastax.astra.cli.db.keyspace;

import com.datastax.astra.cli.core.AbstractInteractiveCmd;
import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.db.OperationsDb;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.DatabaseNotSelectedException;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show Keyspaces for an Database.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsDb.CMD_LIST_KEYSPACES, description = "Display the list of Keyspaces in an database")
public class DbListKeyspacesSh extends AbstractInteractiveCmd {
  
    /** {@inheritDoc} */
    public void execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, 
           DatabaseNotSelectedException, InvalidArgumentException {
        assertDbSelected();
        OperationsDb.listKeyspaces(ctx().getDatabase().getId());
    }

}
