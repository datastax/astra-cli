package com.datastax.astra.shell.cmd.db.keyspace;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.cmd.BaseSh;
import com.datastax.astra.shell.cmd.db.OperationsDb;
import com.datastax.astra.shell.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.shell.exception.DatabaseNotFoundException;
import com.datastax.astra.shell.exception.ParamValidationException;
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
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        if (!dbSelected()) {
            return ExitCode.CONFLICT;
        }
        return OperationsDb.createKeyspace(ShellContext.getInstance().getDatabase().getId(), keyspace, ifNotExist);
    }
    
}
