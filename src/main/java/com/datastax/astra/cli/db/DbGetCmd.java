package com.datastax.astra.cli.db;

import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "get", description = "Show details of a database")
public class DbGetCmd extends AbstractDatabaseCmd {

    /** Enum for db get. */
    public enum DbGetKeys { 
        /** db unique identifier */
        ID,
        /** db status */
        STATUS,
        /** cloud provider */
        CLOUD,
        /** default keyspace */
        KEYSPACE,
        /** all keyspaces */
        KEYSPACES,
        /** default region */
        REGION,
        /** all regions */
        REGIONS
    }
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-k", "--key" }, title = "Key", description = ""
            + "Show value for a property among: "
            + "'id', 'status', 'cloud', 'keyspace', 'keyspaces', 'region', 'regions'")
    protected String key;
    
    /** {@inheritDoc} */
    public void execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {    
        dbServices.showDb(db, key == null ? null : DbGetKeys.valueOf(key.toUpperCase()));
    }

}
