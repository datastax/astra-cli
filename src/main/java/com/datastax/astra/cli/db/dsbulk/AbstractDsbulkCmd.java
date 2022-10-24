package com.datastax.astra.cli.db.dsbulk;

import com.datastax.astra.cli.db.AbstractDatabaseCmd;
import com.github.rvesse.airline.annotations.Option;

/**
 * This command allows loading data with DsBulk.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractDsbulkCmd extends AbstractDatabaseCmd {
    
    /**
     * Target Keyspace
     */
    @Option(name = { "-k", "--keyspace" }, 
            title = "KEYSPACE", 
            description = "Keyspace used for loading or unloading data.")
    protected String keyspace;
    
    /**
     * Target Table
     */
    @Option(name = { "-t", "--table" }, 
            title = "TABLE", 
            description = "Table used for loading or unloading data. "
                    + "Table names should not be quoted and are case-sensitive.")
    protected String table;
    
    /**
     * Optional filter
     */
    @Option(name = { "--schema.query" },
            title = "QUERY", 
            description = "Optional to unload or count")
    protected String query;

    /**
     * Provide parameters in dedicated externalize file
     */
    @Option(name = { "-encoding" },
            title = "ENCODING", 
            description = "The file name format to use when writing. "
                    + "This setting is ignored when reading and for non-file URLs. ")
    protected String encoding = "UTF-8";
    
    /**
     * Optional filter
     */
    @Option(name = { "-maxConcurrentQueries" },
            title = "maxConcurrentQueries", 
            description = "The maximum number of concurrent queries that should be carried in parallel.")
    protected String maxConcurrentQueries = "AUTO";
      
    /**
     * Provide parameters in dedicated externalize file
     */
    @Option(name = { "-logDir" },
            title = "log directory", 
            description = "Optional filter")
    protected String logDir = "./logs";
    
    /**
     * Provide parameters in dedicated externalize file
     */
    @Option(name = { "--dsbulk-config" },
            title = "DSBULK_CONF_FILE", 
            description = "Not all options offered by the loader DSBulk "
                    + "are exposed in this CLI")
    protected String dsBulkConfig;
    
}
