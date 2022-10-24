package com.datastax.astra.cli.db;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Abstraction for DB Commands.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractDatabaseCmd extends AbstractConnectedCmd  {

     /** Access to sdb Services. */
    protected DatabaseService dbServices = DatabaseService.getInstance();
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB", description = "Database name (not unique)")
    protected String db;

    /**
     * Getter accessor for attribute 'db'.
     *
     * @return
     *       current value of 'db'
     */
    public String getDb() {
        return db;
    }
    
    
}
