package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCmd;
import com.datastax.astra.shell.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.shell.exception.DatabaseNotFoundException;
import com.datastax.astra.shell.exception.ParamValidationException;
import com.datastax.astra.shell.utils.CqlShellOptions;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Start CqlSh for a DB.
 * 
 * https://cassandra.apache.org/doc/latest/cassandra/tools/cqlsh.html
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "cqlsh", description = "Start Cqlsh")
public class DbCqlShellCmd extends BaseCmd {

    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB", 
               description = "Database name or identifier")
    public String database;
    
    // -- Cqlsh --
    
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
        CqlShellOptions options = new CqlShellOptions();
        options.setDebug(cqlShOptionDebug);
        options.setEncoding(cqlshOptionEncoding);
        options.setExecute(cqlshOptionExecute);
        options.setFile(cqlshOptionFile);
        options.setKeyspace(cqlshOptionKeyspace);
        options.setVersion(cqlShOptionVersion);
        return OperationsDb.startCqlShell(options, database);
    }
    
}
