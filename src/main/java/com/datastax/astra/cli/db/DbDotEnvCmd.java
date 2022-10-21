package com.datastax.astra.cli.db;

import java.nio.file.Paths;

import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Unforce update of the program.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "create-dotenv", description = "Generate an .env configuration file associate with the db")
public class DbDotEnvCmd extends AbstractDatabaseCmd {
    
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
     * Default keyspace created with the Db
     */
    @Option(name = { "-d", "--directory" }, title = "DIRECTORY", arity = 1, 
            description = "Destination for the config file")
    protected String destination = Paths.get(".").toAbsolutePath().normalize().toString();
    
    /** {@inheritDoc} */
    public void execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException {
        dbServices.generateDotEnvFile(db, keyspace, region, destination);
    }

}
