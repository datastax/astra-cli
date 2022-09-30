package com.datastax.astra.cli.db;

import java.nio.file.Paths;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Unforce update of the program.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "create-dotenv", description = "Generate an .env configuration file associate with the db")
public class DbDotEnvCmd extends AbstractConnectedCmd {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB_NAME", description = "Database name (not unique)")
    protected String db;
    
    /**
     * Cloud provider region to provision
     */
    @Option(name = { "-r", "--region" }, title = "DB_REGION", arity = 1, 
            description = "Cloud provider region to provision")
    protected String region = OperationsDb.DEFAULT_REGION;
    
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
    
    /** {@inheritDoc}  */
    public void execute() throws Exception {
       OperationsDb.generateDotEnvFile(db, keyspace, region, destination);
    }

}
