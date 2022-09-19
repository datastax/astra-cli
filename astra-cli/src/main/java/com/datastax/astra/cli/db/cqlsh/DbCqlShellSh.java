package com.datastax.astra.cli.db.cqlsh;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.BaseSh;
import com.datastax.astra.cli.core.exception.ParamValidationException;
import com.datastax.astra.cli.db.OperationsDb;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Start Cqlsh when a db is selected.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "cqlsh", description = "Start Cqlsh (db must be selected first)")
public class DbCqlShellSh extends BaseSh {
    
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
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        if (!dbSelected()) {
            return ExitCode.CONFLICT;
        }
        CqlShellOptions options = new CqlShellOptions();
        options.setDebug(cqlShOptionDebug);
        options.setEncoding(cqlshOptionEncoding);
        options.setExecute(cqlshOptionExecute);
        options.setFile(cqlshOptionFile);
        options.setKeyspace(cqlshOptionKeyspace);
        options.setVersion(cqlShOptionVersion);
        return OperationsDb.startCqlShell(options, ShellContext.getInstance().getDatabase().getId());
    }

}