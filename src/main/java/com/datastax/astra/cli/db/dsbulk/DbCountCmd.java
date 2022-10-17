package com.datastax.astra.cli.db.dsbulk;

import com.github.rvesse.airline.annotations.Command;

/**
 * Load data into AstraDB.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "count", description = "Count items for a table, a query")
public class DbCountCmd extends AbstractDsbulkDataCmd {
    
    /** {@inheritDoc} */
    @Override
    public void execute()  {
        DsBulkService.getInstance().count(this);
    }


}
