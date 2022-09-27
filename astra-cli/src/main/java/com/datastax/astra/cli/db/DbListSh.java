package com.datastax.astra.cli.db;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseSh;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show Databases for an organization 
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.LIST, description = "Display the list of Databases in an organization")
public class DbListSh extends BaseSh {
   
    /** {@inheritDoc} */
    public void execute() throws Exception {
        OperationsDb.listDb();
    }

}
