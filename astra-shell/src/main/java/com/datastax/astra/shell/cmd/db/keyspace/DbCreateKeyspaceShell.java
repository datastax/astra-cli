package com.datastax.astra.shell.cmd.db.keyspace;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.datastax.astra.shell.cmd.BaseShellCommand;
import com.datastax.astra.shell.cmd.db.OperationsDb;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.CREATE, description = "Create a new keyspace (db must be selected)")
public class DbCreateKeyspaceShell extends BaseShellCommand {
    
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
    public ExitCode execute() {
        if (!dbSelected()) {
            return ExitCode.CONFLICT;
        }
        return OperationsDb.createKeyspace(ShellContext.getInstance().getDatabase().getId(), keyspace, ifNotExist);
    }
    
}
