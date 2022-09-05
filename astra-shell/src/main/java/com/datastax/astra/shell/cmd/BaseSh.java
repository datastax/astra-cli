package com.datastax.astra.shell.cmd;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.shell.exception.DatabaseNotFoundException;
import com.datastax.astra.shell.exception.ParamValidationException;
import com.datastax.astra.shell.out.LoggerShell;
import com.datastax.astra.shell.out.ShellPrinter;

/**
 * Base command.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class BaseSh extends AbstractCmd {
    
    /** {@inheritDoc} */
    public void run() {
       
       // As a shell command it should be initialized
       if (!ctx().isInitialized()) {
           ShellPrinter.outputError(ExitCode.CONFLICT, "A shell command should have the connection set");
       } else {
           ctx().setCurrentShellCommand(this);
           LoggerShell.info("Shell : " + ShellContext.getInstance().getRawShellCommand());
           LoggerShell.info("Class : " + getClass().getName());
           try {
               execute();
           } catch (DatabaseNameNotUniqueException dex) {
               ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, dex.getMessage());
               ExitCode.INVALID_PARAMETER.exit();
           } catch (ParamValidationException pex) {
               ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, pex.getMessage());
               ExitCode.INVALID_PARAMETER.exit();
           } catch (DatabaseNotFoundException nfex) {
               ShellPrinter.outputError(ExitCode.NOT_FOUND, nfex.getMessage());
               ExitCode.NOT_FOUND.exit();
           }
       }
    }
    
    /**
     * Return execution code (CLI).
     * 
     * @exception DatabaseNameNotUniqueException
     *      error with db name
     * @exception DatabaseNotFoundException
     *      when interacting with db, if not found error is thrown
     * @exception ParamValidationException
     *      error with parameters
     * @return
     *      returned code by the command
     */
    public abstract ExitCode execute() 
    throws DatabaseNameNotUniqueException, 
           DatabaseNotFoundException, 
           ParamValidationException;
    
    /**
     * Get current context.
     * 
     * @return
     *      current context
     */
    protected ShellContext ctx() {
        return ShellContext.getInstance();
    }
    
    /**
     * Db Selected.
     * @return
     *      if a db is selected or not
     */
    protected boolean dbSelected() {
        boolean selected = (null != ctx().getDatabase()) ;
        if (!selected) {
            LoggerShell.error("You must select a DB first with 'db use <dbname>'");
        }
        return selected;
    }
    
   
}
