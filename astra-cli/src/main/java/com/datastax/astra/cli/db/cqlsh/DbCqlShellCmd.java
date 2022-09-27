package com.datastax.astra.cli.db.cqlsh;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.db.OperationsDb;
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
public class DbCqlShellCmd extends AbstractConnectedCmd {

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
    
    /** {@inheritDoc} 
     * @throws FileSystemException 
     * @throws CannotStartProcessException */
    public void execute() throws Exception {
        CqlShellOptions options = new CqlShellOptions();
        options.setDebug(cqlShOptionDebug);
        options.setEncoding(cqlshOptionEncoding);
        options.setExecute(cqlshOptionExecute);
        options.setFile(cqlshOptionFile);
        options.setKeyspace(cqlshOptionKeyspace);
        options.setVersion(cqlShOptionVersion);
        OperationsDb.startCqlShell(options, database);
    }
    
}
