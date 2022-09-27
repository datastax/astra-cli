package com.datastax.astra.cli.db.keyspace;

import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.BaseSh;
import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.db.OperationsDb;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.DatabaseNotSelectedException;
import com.datastax.astra.cli.db.exception.KeyspaceAlreadyExistException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsDb.CMD_CREATE_KEYSPACE, description = "Create a new keyspace (db must be selected)")
public class DbCreateKeyspaceSh extends BaseSh {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "KEYSPACE", description = "Name of the keyspace")
    public String keyspace;
    
    /** Cqlsh Options. */
    @Option(name = { "--if-not-exist" }, 
            description = "will create a new DB only if none with same name")
    protected boolean ifNotExist = false;
    
    /** {@inheritDoc} */
    public void execute() 
    throws DatabaseNotSelectedException, DatabaseNameNotUniqueException, 
           DatabaseNotFoundException, InvalidArgumentException, 
           KeyspaceAlreadyExistException {
        assertDbSelected();
        OperationsDb.createKeyspace(
                ShellContext.getInstance().getDatabase().getId(), keyspace, ifNotExist);
    }
    
}
