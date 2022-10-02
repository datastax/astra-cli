package com.datastax.astra.cli.core;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.ConfigurationException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.exception.TokenNotFoundException;
import com.datastax.astra.cli.core.out.OutputFormat;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.DatabaseNotSelectedException;
import com.datastax.astra.cli.db.exception.InvalidDatabaseStateException;
import com.datastax.astra.cli.db.exception.KeyspaceAlreadyExistException;
import com.datastax.astra.cli.iam.exception.RoleNotFoundException;
import com.datastax.astra.cli.iam.exception.UserAlreadyExistException;
import com.datastax.astra.cli.iam.exception.UserNotFoundException;
import com.datastax.astra.cli.streaming.exception.TenantAlreadyExistExcepion;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.datastax.astra.cli.utils.AstraRcUtils;
import com.github.rvesse.airline.annotations.Option;

/**
 * Options, parameters and treatments that you want to apply on all commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractCmd implements Runnable {
    
    /** Command constants. */
    public static final String CREATE     = "create";
    
    /** Command constants. */
    public static final String DELETE     = "delete";
    
    /** Command constants. */
    public static final String GET       = "get";
    
    /** Command constants. */
    public static final String LIST       = "list";
    
    /** Command constants. */
    public static final String USE       = "use";
    
    /** Command constants. */
    public static final String ORG       = "org";
    
    // --- Flags ---
    
    /** 
     * Each command can have a verbose mode. 
     **/
    @Option(name = { "-v","--verbose" }, description = "Verbose mode with log in console")
    protected boolean verbose = false;
    
    /** 
     * Each command can have a verbose mode. 
     **/
    @Option(name = { "--no-color" }, description = "Remove all colors in output")
    protected boolean noColor = false;
    
    /**
     * No log but provide output as a JSON
     */
    @Option(name = { "-o", "--output" }, 
            title = "FORMAT",
            description = "Output format, valid values are: human,json,csv")
    protected OutputFormat output = OutputFormat.human;
    
    /**
     * No log but provide output as a JSON
     */
    @Option(name = { "--log"}, title = "LOG_FILE", description = "Logs will go in the file plus on console")
    protected String logFile;
    
    /**
     * File on disk to reuse configuration.
     */
    @Option(name = { "--config-file" }, 
            title = "CONFIG_FILE",
            description= "Configuration file (default = ~/.astrarc)")
    protected String configFilename = AstraRcUtils.getDefaultConfigurationFileName();
     
    /**
     * Reference to write data into log file.
     */
    protected FileWriter logFileWriter;
    
    /**
     * Initialization of the context.
     * 
     * @throws TokenNotFoundException
     *      token has not been found
     * @throws InvalidTokenException
     *      token provided is invalid
     * @throws FileSystemException
     *      cannot access file system
     */
    public abstract void init()
    throws TokenNotFoundException,
           InvalidTokenException,
           FileSystemException;
    
    /**
     * Return execution code (CLI).
     * 
     * @throws DatabaseNameNotUniqueException
     *      error with db name
     * @throws DatabaseNotFoundException
     *      when interacting with db, if not found error is thrown
     * @throws InvalidArgumentException
     *      error with parameters
     * @throws TenantAlreadyExistExcepion 
     *      tenant was already existing
     * @throws TenantNotFoundException
     *      tenant name as not been found
     * @throws DatabaseNotSelectedException
     *      db was not select before command
     * @throws CannotStartProcessException
     *      cannot start external process (dsbulk, cqlsh..)
     * @throws FileSystemException
     *      reading cloud secure bundle
     * @throws ConfigurationException
     *      error with configuration
     * @throws KeyspaceAlreadyExistException
     *      keyspace already exist
     * @throws TokenNotFoundException
     *      no token provided
     * @throws InvalidTokenException
     *      token provided has invalid format.
     * @throws InvalidDatabaseStateException
     *      invalid statie when running a command
     * @throws RoleNotFoundException
     *      role has not be found
     * @throws UserNotFoundException
     *      user has not be found
     * @throws UserAlreadyExistException
     *      user has not be found
     */
    public abstract void execute() 
    throws CannotStartProcessException, 
           ConfigurationException,
           DatabaseNameNotUniqueException, 
           DatabaseNotFoundException, 
           DatabaseNotSelectedException, 
           DatabaseNameNotUniqueException,
           FileSystemException, 
           InvalidArgumentException, 
           KeyspaceAlreadyExistException,
           TenantAlreadyExistExcepion, 
           TenantNotFoundException,
           TokenNotFoundException, 
           InvalidTokenException,
           RoleNotFoundException,
           UserNotFoundException, 
           UserAlreadyExistException, 
           InvalidDatabaseStateException;
    
    /** {@inheritDoc} */
    public void run() {}
    
    /**
     * Initialize logger when needed.
     * 
     * @throws FileSystemException
     *      error accessing file system
     */
    public void initLog() 
    throws FileSystemException {
        if (!StringUtils.isEmpty(logFile)) {
            try {
               logFileWriter = new FileWriter(logFile, true);
            } catch (IOException e) {
               throw new FileSystemException("Cannot initialize log file " + logFile + " " + e.getMessage(), e);
            }
        }
    }
    
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
     * Getter accessor for attribute 'debug'.
     *
     * @return
     *       current value of 'debug'
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Getter accessor for attribute 'noColor'.
     *
     * @return
     *       current value of 'noColor'
     */
    public boolean isNoColor() {
        return noColor;
    }    
    
    /**
     * Enable flag.
     * 
     * @param <T>
     *      sub class of asbtract
     * @return
     *      current reference
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractCmd> T verbose() {
        this.verbose = true;
        return (T) this;
    }

    /**
     * Enable flag.
     * 
     * @param <T>
     *      sub class of asbtract
     * @return
     *      current reference
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractCmd> T noColor() {
        this.noColor = true;
        return (T) this;
    }
    
    /**
     * Getter accessor for attribute 'format'.
     *
     * @return
     *       current value of 'format'
     */
    public OutputFormat getOutput() {
        return output;
    }

    /**
     * Change options format.
     *
     * @param output
     *      update output
     * @return
     *      current reference
     */
    public AbstractCmd output(OutputFormat output) {
        this.output = output;
        return this;
    }

    /**
     * Getter accessor for attribute 'configFilename'.
     *
     * @return
     *       current value of 'configFilename'
     */
    public String getConfigFilename() {
        return configFilename;
    }
    
    /**
     * Change options format.
     * 
     * @param conf
     *      update configuration
     * @return
     *      current reference
     */
    public AbstractCmd configFilename(String conf) {
        this.configFilename = conf;
        return this;
    }
    
    /**
     * Getter accessor for attribute 'logFile'.
     *
     * @return
     *       current value of 'logFile'
     */
    public String getLogFile() {
        return logFile;
    }
    
    /**
     * Change options format.
     * 
     * @param plogFile
     *      loggin file
     * @return
     *      current reference
     */
    public AbstractCmd logFile(String plogFile) {
        this.configFilename = plogFile;
        return this;
    }

    /**
     * Getter accessor for attribute 'logFileWriter'.
     *
     * @return
     *       current value of 'logFileWriter'
     */
    public FileWriter getLogFileWriter() {
        return logFileWriter;
    }
    
}
