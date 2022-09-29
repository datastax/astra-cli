package com.datastax.astra.cli;

import static com.datastax.astra.cli.ExitCode.CANNOT_CONNECT;
import static com.datastax.astra.cli.ExitCode.INVALID_PARAMETER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.core.BaseSh;
import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.exception.TokenNotFoundException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.out.OutputFormat;
import com.datastax.astra.cli.core.out.ShellPrinter;
import com.datastax.astra.cli.utils.AstraRcUtils;
import com.datastax.astra.sdk.config.AstraClientConfig;
import com.datastax.astra.sdk.databases.DatabasesClient;
import com.datastax.astra.sdk.databases.domain.Database;
import com.datastax.astra.sdk.organizations.OrganizationsClient;
import com.datastax.astra.sdk.organizations.domain.Organization;
import com.datastax.astra.sdk.streaming.StreamingClient;

/**
 * Hold the context of CLI to know where we are.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class ShellContext {

    /**
     * Singleton Pattern, private intance.
     */
    private static ShellContext _instance;
    
    /**
     * Default Constructor for Shell.
     */
    private ShellContext() {}
    
    /**
     * Singleton Pattern.
     *
     * @return
     *      current instance of context
     */
    public static synchronized ShellContext getInstance() {
        if (_instance == null) {
            _instance = new ShellContext();
        }
        return _instance;
    }
    
    // -- Config --
    
    /** Current token in use */
    private String token;
    
    /** Configuration file name. */
    private String configFilename = AstraRcUtils.getDefaultConfigurationFileName();
    
    /** Configuration section. */
    private String configSection = AstraRcUtils.ASTRARC_DEFAULT;
    
    /** Load Astra Rc in the context. */
    private AstraRcUtils astraRc;
     
    // -- Clients --
    
    /** Hold a reference for the Api Devops. */
    private DatabasesClient apiDevopsDatabases;
    
    /** Hold a reference for the Api Devops. */
    private OrganizationsClient apiDevopsOrganizations;
    
    /** Hold a reference for the Api Devops. */
    private StreamingClient apiDevopsStreaming;
    
    // -- Selection --
    
    /** Current command. */
    private AbstractCmd startCommand;
    
    /** Current shell command (overriding Cli eventually). */
    private BaseSh currentShellCommand;
    
    /** Raw command. */
    private List<String> rawCommand = new ArrayList<>();
    
    /** Raw command. */
    private String rawShellCommand;
    
    /** History of commands in shell. */
    private List<BaseSh> history = new ArrayList<>();
    
    /** Organization informations (prompt). */
    private Organization organization;
    
    /** Work on a db. */
    private Database database;
    
    /** Database informations. */
    private String databaseRegion;
    
    /**
     * Init AstraRc.
     *
     * @param configFileName
     *      configuration file name
     */
    private void initAstraRc(String configFileName) {
        if (configFileName == null) {
            configFileName = AstraRcUtils.getDefaultConfigurationFileName();
        }
        LoggerShell.debug("ConfigFilename: " + configFileName);
        this.astraRc = new AstraRcUtils(configFileName);
    }
    
    /**
     * Should initialized the client based on provided parameters.
     *
     * @param cli
     *      command line cli
     * @throws TokenNotFoundException 
     *      cannot initialize if not token
     * @throws InvalidTokenException 
     *      invalid token providede,
     */
    public void init(AbstractCmd cli) 
    throws TokenNotFoundException, InvalidTokenException {
        
        this.startCommand = cli;
        initAstraRc(cli.getConfigFilename());
        
        LoggerShell.info("-----------------------------------------------------");
        LoggerShell.info("Command : " + ShellContext.getInstance().getRawCommandString());
        LoggerShell.info("Class   : " + cli.getClass());
        
        if (cli instanceof AbstractConnectedCmd) {
            AbstractConnectedCmd ccli = (AbstractConnectedCmd) cli;
            this.token  = ccli.getToken();
            if (this.token == null) {
                if (!StringUtils.isEmpty(ccli.getConfigSectionName())) {
                    this.configSection = ccli.getConfigSectionName();
                    LoggerShell.debug("ConfigSectionName: " + configSection);
                }
                assertSection(ccli);
                assertTokenInSection(ccli);
                token = this.astraRc
                            .getSection(this.configSection)
                            .get(AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN);
            }
            
            if (token != null) {
                LoggerShell.debug("Token: " + token.substring(0, 20) + "...");
                connect(token);
            }
        }
    }
    
    /**
     * Valid section.
     *
     * @param cmd
     *      current command with options
     * @return
     *      section is valid
     * @throws TokenNotFoundException
     *      token not found in section
     */
    private void assertSection(AbstractCmd cmd) 
    throws TokenNotFoundException {
        if (!this.astraRc.isSectionExists(this.configSection)) {
            ShellPrinter.outputError(CANNOT_CONNECT, "No token provided (-t), no config provided (--config), section '" + this.configSection 
                    + "' has not been found in config file '" 
                    + this.astraRc.getConfigFile().getPath() + "'. Try [astra setup]");
           throw new TokenNotFoundException();
        }
    }
    
    /**
     * Log error.
     *
     * @param cmd
     *      command.
     * @return
     *      error
     * @throws TokenNotFoundException
     *      token not found
     */
    private void assertTokenInSection(AbstractCmd cmd) 
    throws TokenNotFoundException {
        if (StringUtils.isEmpty(this.astraRc
                .getSection(this.configSection)
                .get(AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN))) {
            ShellPrinter.outputError(
                    INVALID_PARAMETER, 
                    "Key '" + AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN + 
                    "' has not found been in config [section '" + this.configSection + "']");
            throw new TokenNotFoundException();
        }
    }
    
    /**
     * Based on a token will initialize the connection.
     * 
     * @param token
     *      token loaded from param
     * @throws InvalidTokenException 
     */
    public void connect(String token) 
    throws InvalidTokenException {

        // Persist Token
        this.token = token;
        
        if (!token.startsWith(token)) {
            ShellPrinter.outputError(INVALID_PARAMETER, "Token provided is invalid. It should start with 'AstraCS:...'. Try [astra setup]");
            throw new InvalidTokenException(token);
        }

        apiDevopsOrganizations  = new OrganizationsClient(token);
        apiDevopsDatabases = new DatabasesClient(token);  
        apiDevopsStreaming = new StreamingClient(token);
        
        try {
            this.organization = apiDevopsOrganizations.organization();
            LoggerShell.info("Cli successfully initialized");
        } catch(Exception e) {
            ShellPrinter.outputError(CANNOT_CONNECT, "Token provided is invalid. Try [astra setup]");
            throw new InvalidTokenException(token);
        }
    }
    
    /**
     * Setter accessor for attribute 'database'.
     * @param database
     *      new value for 'database '
     */
    public void useDatabase(Database database) {
        this.database = database;
        // Initialize to default region
        this.databaseRegion = this.database.getInfo().getRegion();
    }
    
    /**
     * Setter accessor for attribute 'databaseRegion'.
     * @param databaseRegion
     *      new value for 'databaseRegion '
     */
    public void useRegion(String databaseRegion) {
        this.databaseRegion = databaseRegion;
    }
    
    /**
     * Reference if the context has been initialized.
     * 
     * @return
     *      if context is initialized
     */
    public boolean isInitialized() {
        return getToken() != null;
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
     * Getter accessor for attribute 'organization'.
     *
     * @return
     *       current value of 'organization'
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * Getter accessor for attribute 'database'.
     *
     * @return
     *       current value of 'database'
     */
    public Database getDatabase() {
        return database;
    }
    
    /**
     * Getter accessor for attribute 'databaseRegion'.
     *
     * @return
     *       current value of 'databaseRegion'
     */
    public String getDatabaseRegion() {
        return databaseRegion;
    }
    
    /**
     * Drop focus on current database.
     */
    public void exitDatabase() {
        this.database = null;
        this.databaseRegion = null;
    }

    /**
     * user flag as no color to get a fixed size output.
     * 
     * @return
     *      if no color flag is toggled.
     */
    public boolean isNoColor() {
        BaseSh sh = getCurrentShellCommand();
        if (sh != null) return sh.isNoColor();
        return getStartCommand().isNoColor();
    }
    
    /**
     * Log in the console only if verbose is enabled.
     *
     * @return
     *      if verbose
     */
    public boolean isVerbose() {
        BaseSh sh = getCurrentShellCommand();
        if (sh != null) return sh.isVerbose();
        return getStartCommand().isVerbose();
    }
    
    /**
     * Trigger a log only if relevant.
     * 
     * @return
     *      check if logger is enabled
     */
    public boolean isFileLoggerEnabled() {
        BaseSh sh = getCurrentShellCommand();
        if (sh != null) return (sh.getLogFileWriter() != null);
        return getStartCommand().getLogFileWriter() != null;
    }
    
    /**
     * Retrieve output format based on raw command.
     *
     * @return
     *      output format
     */
    public OutputFormat getOutputFormat() {
        BaseSh sh = getCurrentShellCommand();
        if (sh != null) return sh.getOutput();
        return getStartCommand().getOutput();
    }
    
    /**
     * Getter accessor for attribute 'apiDevopsDatabases'.
     *
     * @return
     *       current value of 'apiDevopsDatabases'
     */
    public DatabasesClient getApiDevopsDatabases() {
        return apiDevopsDatabases;
    }

    /**
     * Getter accessor for attribute 'apiDevopsStreaming'.
     *
     * @return
     *       current value of 'apiDevopsStreaming'
     */
    public StreamingClient getApiDevopsStreaming() {
        return apiDevopsStreaming;
    }
    
    /**
     * Getter accessor for attribute 'apiDevopsOrganizations'.
     *
     * @return
     *       current value of 'apiDevopsOrganizations'
     */
    public OrganizationsClient getApiDevopsOrganizations() {
        return apiDevopsOrganizations;
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
     * Getter accessor for attribute 'configSection'.
     *
     * @return
     *       current value of 'configSection'
     */
    public String getConfigSection() {
        return configSection;
    }

    /**
     * Getter accessor for attribute 'astraRc'.
     *
     * @return
     *       current value of 'astraRc'
     */
    public AstraRcUtils getAstraRc() {
        return astraRc;
    }

    /**
     * Getter accessor for attribute 'startCommand'.
     *
     * @return
     *       current value of 'startCommand'
     */
    public AbstractCmd getStartCommand() {
        return startCommand;
    }
    
    /**
     * Getter accessor for attribute 'currentShellCommand'.
     *
     * @return
     *       current value of 'currentShellCommand'
     */
    public BaseSh getCurrentShellCommand() {
        return currentShellCommand;
    }

    /**
     * Setter accessor for attribute 'currentShellCommand'.
     * @param currentShellCommand
     * 		new value for 'currentShellCommand '
     */
    public void setCurrentShellCommand(BaseSh currentShellCommand) {
        this.currentShellCommand = currentShellCommand;
        this.history.add(currentShellCommand);
    }

    /**
     * Getter accessor for attribute 'history'.
     *
     * @return
     *       current value of 'history'
     */
    public List<BaseSh> getHistory() {
        return history;
    }

    /**
     * Getter accessor for attribute 'rawCommand'.
     *
     * @return
     *       current value of 'rawCommand'
     */
    public List<String> getRawCommand() {
        return rawCommand;
    }

    /**
     * Setter accessor for attribute 'rawCommand'.
     *
     * @param args
     * 		input arguments for the command.
     */
    public void setRawCommand(String... args) {
        this.rawCommand = Arrays.asList(args);
    }
    
    /**
     * Get Current command as a String.
     *
     * @return
     *      current command as a String
     */
    public String getRawCommandString() {
        return "astra " + StringUtils.join(getRawCommand(), " ");
    }

    /**
     * Getter accessor for attribute 'rawShellCommand'.
     *
     * @return
     *       current value of 'rawShellCommand'
     */
    public String getRawShellCommand() {
        return rawShellCommand;
    }

    /**
     * Setter accessor for attribute 'rawShellCommand'.
     * @param rawShellCommand
     * 		new value for 'rawShellCommand '
     */
    public void setRawShellCommand(String rawShellCommand) {
        this.rawShellCommand = rawShellCommand;
    }
  
}
