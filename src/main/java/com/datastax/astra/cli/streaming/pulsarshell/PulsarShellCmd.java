package com.datastax.astra.cli.streaming.pulsarshell;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.streaming.OperationsStreaming;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * This command allows to load data with pulsar-client.
 * 
 * 
 * astra pulsar-shell
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "pulsar-shell", description = "Start pulsar admin against your tenant")
public class PulsarShellCmd extends AbstractConnectedCmd {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "TENANT", description = "Tenant unique name")
    public String tenant;
    
    /** Options. */
    @Option(name = {"-e", "--execute-command" }, title = "command", arity = 1,  
            description = "Execute the statement and quit.")
    protected String execute;
   
    /** Cqlsh Options. */
    @Option(name= {"--fail-on-error"}, 
            description= "If true, the shell will be interrupted if a command throws an exception.")
    protected boolean failOnError = false;
    
    /** Cqlsh Options. */
    @Option(name = {"-f", "--filename" }, title = "FILE", arity = 1,  
            description = "Input filename with a list of commands to be executed. Each command must be separated by a newline.")
    protected String fileName;
    
    /** Cqlsh Options. */
    @Option(name=  {"-np", "--no-progress" }, 
            description= "Display raw output of the commands without the fancy progress visualization. ")
    protected boolean noProgress = false;
    
    /** {@inheritDoc} */
    public void execute() 
    throws TenantNotFoundException, CannotStartProcessException, FileSystemException {
        PulsarShellOptions options = new PulsarShellOptions();
        options.setExecute(execute);
        options.setFailOnError(failOnError);
        options.setFileName(fileName);
        options.setNoProgress(noProgress);
        OperationsStreaming.startPulsarShell(options, tenant);
    }
    
}
