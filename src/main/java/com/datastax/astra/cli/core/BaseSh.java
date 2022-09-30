package com.datastax.astra.cli.core;

import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.exception.TokenNotFoundException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.exception.DatabaseNotSelectedException;

/**
 * Base command.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class BaseSh extends AbstractCmd {
    
    /** {@inheritDoc} */
    @Override
    public void init() 
    throws TokenNotFoundException, InvalidTokenException {
        if (!ctx().isInitialized()) {
            throw new TokenNotFoundException();
        }
        ctx().setCurrentShellCommand(this);
        LoggerShell.info("Shell : " + ShellContext.getInstance().getRawShellCommand());
        LoggerShell.info("Class : " + getClass().getName());
    }
   
    /**
     * Db Selected.
     *
     * @throws DatabaseNotSelectedException
     *      db not selected 
     */
    protected void assertDbSelected() throws DatabaseNotSelectedException {
        if (null == ctx().getDatabase()) {
            throw new DatabaseNotSelectedException();
        }
    }    
   
}
