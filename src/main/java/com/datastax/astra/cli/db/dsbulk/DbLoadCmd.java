package com.datastax.astra.cli.db.dsbulk;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Load data into AstraDB.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "load", description = "Load data leveraging DSBulk")
public class DbLoadCmd extends AbstractDsbulkDataCmd {
    
    /**
     * Optional filter
     */
    @Option(name = { "-dryRun" },
            title = "dryRun", 
            description = "Enable or disable dry-run mode, a test mode that runs the "
                    + "command but does not load data. ")
    boolean dryRun = false;
    
    /** {@inheritDoc} */
    @Override
    public void execute()  {
        DsBulkService.getInstance().load(this);
    }

}
