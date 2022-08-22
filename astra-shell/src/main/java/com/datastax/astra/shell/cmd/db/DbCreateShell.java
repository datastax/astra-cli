package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.datastax.astra.shell.cmd.BaseShellCommand;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * db create NAME -r eu-east1 -ks ks1
 * 
 * @see DbCreateShell
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.CREATE, description = "Create a database with shell")
public class DbCreateShell extends BaseShellCommand {
    
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
    
    /** {@inheritDoc} */
    @Override
    public ExitCode execute() {
        return OperationsDb.createDb(databaseName, databaseRegion, defaultKeyspace, ifNotExist);
    }
    
}
