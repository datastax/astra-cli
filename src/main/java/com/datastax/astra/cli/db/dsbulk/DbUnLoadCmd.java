package com.datastax.astra.cli.db.dsbulk;

import com.github.rvesse.airline.annotations.Command;

/**
 * Load data into AstraDB.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "unload", 
        description = "Unload data leveraging DSBulk")
public class DbUnLoadCmd extends AbstractDsbulkDataCmd {
    
    /** {@inheritDoc} */
    @Override
    public void execute()  {
        DsBulkService.getInstance().unload(this);
    }

}
