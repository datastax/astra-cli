package com.datastax.astra.cli.core;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.exception.ParamValidationException;
import com.datastax.astra.cli.core.out.ShellPrinter;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.streaming.exception.TenantAlreadyExistExcepion;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.datastax.astra.cli.utils.AstraRcUtils;
import com.github.rvesse.airline.annotations.Option;

/**
 * Base command for cli. The cli have to deal with configuration file and initialize connection
 * each tiem where shell already have the context initialized.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class BaseCmd extends BaseSh {
    
    // --- Authentication ---
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "--token" }, 
            title = "AUTH_TOKEN",
            description = "Key to use authenticate each call.")
    protected String token;

    /**
     * File on disk to reuse configuration.
     */
    @Option(name = { "--config-file" }, 
            title = "CONFIG_FILE",
            description= "Configuration file (default = ~/.astrarc)")
    protected String configFilename = AstraRcUtils.getDefaultConfigurationFileName();
  
    /**
     * Section.
     */
    @Option(name = { "-conf","--config" }, 
            title = "CONFIG_SECTION",
            description= "Section in configuration file (default = ~/.astrarc)")
    protected String configSectionName = AstraRcUtils.ASTRARC_DEFAULT;
    
    /**
     * No log but provide output as a JSON
     */
    @Option(name = { "--log"}, title = "LOG_FILE", description = "Logs will go in the file plus on console")
    protected String logFile;
    
    /**
     * Reference to write data into log file.
     */
    protected FileWriter logFileWriter;
    
    /**
     * If logger is required, write in dedicated file.
     */
    private void initLogFile() {
        if (!StringUtils.isEmpty(logFile)) {
            try {
               logFileWriter = new FileWriter(logFile, true);
            } catch (IOException e) {
               System.out.println("[ERROR] - Cannot open log file " + logFile + ":" + e.getMessage());
               ExitCode.INVALID_PARAMETER.exit();
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void run() {
        // Initialization of Logger
       initLogFile();
        
       // Initialization of context
       ShellContext.getInstance().init(this);
       
       // Execute command and exit program
       
       try {
           execute().exit();
       } catch (DatabaseNameNotUniqueException dex) {
           ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, dex.getMessage());
           ExitCode.INVALID_PARAMETER.exit();
       } catch (ParamValidationException pex) {
           ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, pex.getMessage());
           ExitCode.INVALID_PARAMETER.exit();
       }  catch (DatabaseNotFoundException nfex) {
           ShellPrinter.outputError(ExitCode.NOT_FOUND, nfex.getMessage());
           ExitCode.NOT_FOUND.exit();
       } catch (TenantAlreadyExistExcepion e) {
           ShellPrinter.outputError(ExitCode.ALREADY_EXIST, e.getMessage());
           ExitCode.ALREADY_EXIST.exit();
       } catch (TenantNotFoundException e) {
           ShellPrinter.outputError(ExitCode.NOT_FOUND, e.getMessage());
           ExitCode.NOT_FOUND.exit();
       }
    }
    
    /**
     * Getter accessor for attribute 'token'.
     *
     * @return
     *       current value of 'token'
     */
    public String getToken() {
        return token;
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
     * Getter accessor for attribute 'configSectionName'.
     *
     * @return
     *       current value of 'configSectionName'
     */
    public String getConfigSectionName() {
        return configSectionName;
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
     * Getter accessor for attribute 'logFileWriter'.
     *
     * @return
     *       current value of 'logFileWriter'
     */
    public FileWriter getLogFileWriter() {
        return logFileWriter;
    }
   
   
}
