package com.datastax.astra.cli.db;

import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.InvalidDatabaseStateException;
import com.datastax.astra.cli.db.exception.KeyspaceAlreadyExistException;
import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Create a DB with the CLI (initializing connection)
 *
 * astra db create NAME -r eu-east1 -ks ks1
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "create", description = "Create a database with cli")
public class DbCreateCmd extends AbstractDatabaseCmd {
    
    /** 
     * Database or keyspace are created when needed
     **/
    @Option(name = { "--if-not-exist", "--if-not-exists" }, 
            description = "will create a new DB only if none with same name")
    protected boolean ifNotExist = false;

    /**
     * Cloud provider region to provision
     */
    @Option(name = { "-r", "--region" }, title = "DB_REGION", arity = 1, 
            description = "Cloud provider region to provision")
    protected String region = DatabaseService.DEFAULT_REGION;
    
    /**
     * Default keyspace created with the Db
     */
    @Option(name = { "-k", "--keyspace" }, title = "KEYSPACE", arity = 1, 
            description = "Default keyspace created with the Db")
    protected String keyspace;
    
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
    public void execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, 
           InvalidDatabaseStateException, InvalidArgumentException, KeyspaceAlreadyExistException  {
        dbServices.createDb(db, region, keyspace, ifNotExist);
        if (wait) {
            switch (dbServices.waitForDbStatus(db, DatabaseStatusType.ACTIVE, timeout)) {
                case NOT_FOUND -> throw new DatabaseNotFoundException(db);
                case UNAVAILABLE -> throw new InvalidDatabaseStateException(db, DatabaseStatusType.ACTIVE, DatabaseStatusType.PENDING);
                default -> LoggerShell.success("Database '%s' has been created.".formatted(db));
            }
        }
    }
    
}
