package com.datastax.astra.cli.core;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.ConfigurationException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.exception.TokenNotFoundException;
import com.datastax.astra.cli.core.out.OutputFormat;
import com.datastax.astra.cli.core.out.ShellPrinter;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.DatabaseNotSelectedException;
import com.datastax.astra.cli.db.exception.KeyspaceAlreadyExistException;
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
     * Decide if exit after running.
     */
    protected boolean exit = true;
     
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
     * @return
     *      returned code by the command
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
     * @throws Exception
     *      generic exception
     * @throws DatabaseNotSelectedException
     *      db was not select before command
     * @throws CannotStartProcessException
     *      cannot start external process (dsbulk, cqlsh..)
     * @throws FileSystemException
     *      reading cloud secure bundle
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
           Exception; 
    
    /** {@inheritDoc} */
    public void run() {
        ExitCode code = runCmd();
        if (exit) {
            System.exit(code.getCode());
        }
    }
    
    /**
     * Run command without exiting.
     *
     * @return
     *      execution code
     */
    public ExitCode runCmd() {
        ExitCode code = ExitCode.SUCCESS;
        try {
            initLog();
            init();
            execute();
        } catch (DatabaseNameNotUniqueException dex) {
            ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, dex.getMessage());
            code =  ExitCode.INVALID_PARAMETER;
        } catch (InvalidArgumentException pex) {
            ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, pex.getMessage());
            code =  ExitCode.INVALID_PARAMETER;
        } catch (DatabaseNotFoundException nfex) {
            ShellPrinter.outputError(ExitCode.NOT_FOUND, nfex.getMessage());
            code = ExitCode.NOT_FOUND;
        } catch (TenantAlreadyExistExcepion e) {
            ShellPrinter.outputError(ExitCode.ALREADY_EXIST, e.getMessage());
            code = ExitCode.ALREADY_EXIST;
        } catch (TenantNotFoundException e) {
            ShellPrinter.outputError(ExitCode.NOT_FOUND, e.getMessage());
            code =  ExitCode.NOT_FOUND;
        } catch (InvalidTokenException e) {
            ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, e.getMessage());
            code =  ExitCode.INVALID_PARAMETER;
        } catch (FileSystemException e) {
            ShellPrinter.outputError(ExitCode.CONFIGURATION, e.getMessage());
            code =  ExitCode.CONFIGURATION;
        } catch (DatabaseNotSelectedException e) {
            ShellPrinter.outputError(ExitCode.ILLEGAL_STATE, e.getMessage());
            code =  ExitCode.ILLEGAL_STATE;
        } catch (ConfigurationException ex) {
            ShellPrinter.outputError(ExitCode.CONFIGURATION, ex.getMessage());
            code =  ExitCode.CONFIGURATION;
        } catch (Exception e) {
            ShellPrinter.outputError(ExitCode.INTERNAL_ERROR, e.getMessage());
            code =  ExitCode.INTERNAL_ERROR;
        }
        return code;
    }
    
    /**
     * Initialize logger when needed.
     * 
     * @throws FileSystemException
     *      error accessing file system
     */
    private void initLog() 
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
     * Getter accessor for attribute 'exit'.
     *
     * @return
     *       current value of 'exit'
     */
    public boolean shouldExit() {
        return exit;
    }

    /**
     * Enable flag.
     * 
     * @return
     *      current reference
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractCmd> T exit(boolean shouldExit) {
        this.exit = shouldExit;
        return (T) this;
    }
    
    /**
     * Enable flag.
     * 
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
