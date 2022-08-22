package com.datastax.astra.shell.cmd;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.out.LoggerShell;
import com.datastax.astra.shell.out.ShellPrinter;

/**
 * Base command.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class BaseShellCommand extends BaseCommand {
    
    /** {@inheritDoc} */
    public void run() {
       
       // As a shell command it should be initialized
       if (!ctx().isInitialized()) {
           ShellPrinter.outputError(ExitCode.CONFLICT, "A shell command should have the connection set");
       } else {
           ctx().setCurrentShellCommand(this);
           LoggerShell.info("Shell : " + ShellContext.getInstance().getRawShellCommand());
           LoggerShell.info("Class : " + getClass().getName());
           execute();
       }
    }
    
    /**
     * Return execution code (CLI).
     * 
     * @return
     *      returned code by the command
     */
    public abstract ExitCode execute();
    
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
