package com.datastax.astra.cli.db;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseSh;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.InvalidDatabaseStateException;
import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * db create NAME -r eu-east1 -ks ks1
 * 
 * @see DbCreateSh
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.CREATE, description = "Create a database with shell")
public class DbCreateSh extends BaseSh {
    
    /** Cqlsh Options. */
    @Option(name = { "--if-not-exist" }, 
            description = "will create a new DB only if none with same name")
    protected boolean ifNotExist = false;
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB_NAME", description = "Database name (not unique)")
    protected String databaseName;
    
    /**
     * Cloud provider region to provision
     */
    @Option(name = { "-r", "--region" }, title = "DB_REGION", arity = 1, 
            description = "Cloud provider region to provision")
    protected String databaseRegion = OperationsDb.DEFAULT_REGION;
    
    /**
     * Default keyspace created with the Db
     */
    @Option(name = { "-ks", "--keyspace" }, title = "KEYSPACE", arity = 1, 
            description = "Default keyspace created with the Db")
    protected String defaultKeyspace;
    
    /** 
     * Will wait until the database become ACTIVE.
     */
    @Option(name = { "--wait" }, 
            description = "Will wait until the database become ACTIVE")
    protected boolean wait = false;
    
    /** 
     * Provide a limit to the wait period in seconds, default is 180s. 
     */
    @Option(name = { "--timeout" }, 
            description = "Provide a limit to the wait period in seconds, default is 180s.")
    protected int timeout = 180;
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws Exception {
        OperationsDb.createDb(databaseName, databaseRegion, defaultKeyspace, ifNotExist);
        if (wait) {
            switch(OperationsDb.waitForDbStatus(databaseName, DatabaseStatusType.ACTIVE, timeout)) {
                case NOT_FOUND:
                    throw new DatabaseNotFoundException(databaseName);
                case UNAVAILABLE:
                    throw new InvalidDatabaseStateException(databaseName, DatabaseStatusType.ACTIVE,  DatabaseStatusType.PENDING);
                default:
                    LoggerShell.success("Database \'" + databaseName +  "' has been created.");
                break;  
            }
        }
    }
    
}
