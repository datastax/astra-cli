package com.datastax.astra.cli.db.keyspace;

import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.db.AbstractDatabaseCmd;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.KeyspaceAlreadyExistException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "create-keyspace", description = "Create a new keyspace")
public class DbCreateKeyspaceCmd extends AbstractDatabaseCmd {
   
    /** Provide a keyspace Name. */
    @Required
    @Option(name = {"-k", "--keyspace" }, 
            title = "KEYSPACE", 
            arity = 1,  
            description = "Name of the keyspace to create")
    public String keyspace;
    
    /** Cqlsh Options. */
    @Option(name = { "--if-not-exist" }, 
            description = "will create a new DB only if none with same name")
    protected boolean ifNotExist = false;
    
    /** {@inheritDoc}  */
    public void execute() {
        dbServices.createKeyspace(db, keyspace, ifNotExist);
    }
    
}
