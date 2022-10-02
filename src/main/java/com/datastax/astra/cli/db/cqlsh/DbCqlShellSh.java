package com.datastax.astra.cli.db.cqlsh;

import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.AbstractInteractiveCmd;
import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.db.OperationsDb;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.DatabaseNotSelectedException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Start Cqlsh when a db is selected.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "cqlsh", description = "Start Cqlsh (db must be selected first)")
public class DbCqlShellSh extends AbstractInteractiveCmd {
    
    /** Cqlsh Options. */
    @Option(name = { "--version" }, 
            description = "Display information of cqlsh.")
    protected boolean cqlShOptionVersion = false;
    
    /** Cqlsh Options. */
    @Option(name= {"--debug"}, 
            description= "Show additional debugging information.")
    protected boolean cqlShOptionDebug = false;
    
    /** Cqlsh Options. */
    @Option(name = {"--encoding" }, title = "ENCODING", arity = 1,  
            description = "Output encoding. Default encoding: utf8.")
    protected String cqlshOptionEncoding;
    
    /** Cqlsh Options. */
    @Option(name = {"-e", "--execute" }, title = "STATEMENT", arity = 1,  
            description = "Execute the statement and quit.")
    protected String cqlshOptionExecute;
    
    /** Cqlsh Options. */
    @Option(name = {"-f", "--file" }, title = "FILE", arity = 1,  
            description = "Execute commands from a CQL file, then exit.")
    protected String cqlshOptionFile;
    
    /** Cqlsh Options. */
    @Option(name = {"-k", "--keyspace" }, title = "KEYSPACE", arity = 1,  
            description = "Authenticate to the given keyspace.")
    protected String cqlshOptionKeyspace;
    
    /** {@inheritDoc}  */
    public void execute() 
    throws DatabaseNotSelectedException, DatabaseNameNotUniqueException, 
           DatabaseNotFoundException, CannotStartProcessException, FileSystemException {
        assertDbSelected();
        CqlShellOption options = new CqlShellOption(
                cqlShOptionVersion, cqlShOptionDebug, cqlshOptionEncoding,
                cqlshOptionExecute,cqlshOptionFile,cqlshOptionKeyspace);
        
        OperationsDb.startCqlShell(options, ShellContext.getInstance().getDatabase().getId());
    }

}
