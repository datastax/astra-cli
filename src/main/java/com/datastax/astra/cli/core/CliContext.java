package com.datastax.astra.cli.core;

import static com.datastax.astra.sdk.config.AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.datastax.astra.cli.config.AstraConfiguration;
import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.exception.TokenNotFoundException;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.out.OutputFormat;
import com.datastax.astra.sdk.databases.DatabasesClient;
import com.datastax.astra.sdk.organizations.OrganizationsClient;
import com.datastax.astra.sdk.streaming.StreamingClient;

/**
 * Hold the context of CLI to know where we are.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class CliContext {

    /**
     * Singleton Pattern, private intance.
     */
    private static CliContext instance;
    
    /**
     * Default Constructor for Shell.
     */
    private CliContext() {}
    
    /**
     * Singleton Pattern.
     *
     * @return
     *      current instance of context
     */
    public static synchronized CliContext getInstance() {
        if (instance == null) {
            instance = new CliContext();
        }
        return instance;
    }
    
    /** Options. */
    private List<String> arguments;
    
    /** Options. */
    private CoreOptions coreOptions;
    
    /** Options. */
    private TokenOptions tokenOptions;
    
    /** Configuration. */
    private AstraConfiguration astraConfig;
     
    /**
     * Should initialized the client based on provided parameters.
     *
     * @param options
     *      options of the cli
     */
    public void init(CoreOptions options)  {
        this.coreOptions = options;
        this.astraConfig = new AstraConfiguration(coreOptions.configFilename());
    }
    
    /**
     * Init Token and setup connection.
     * 
     * @param options
     *      target options
     */
    public void initToken(TokenOptions options) {
        this.tokenOptions = options;
        if (null == this.tokenOptions.token()) {
            updateTokenWithSectionValue();
        }
        validateToken();
    }
    
    /**
     * Access if color is provided or not
     * 
     * @return
     *    no color
     */
    public boolean isNoColor() {
        return coreOptions != null && coreOptions.noColor();
    }
    
    /**
     * Access if verbose is active or not
     * 
     * @return
     *    no color
     */
    public boolean isVerbose() {
        return coreOptions != null && coreOptions.verbose();
    }
    
    /**
     * Access if extra configuration file provided
     * 
     * @return
     *    configuration file
     */
    public String getConfigFilename() {
        if (coreOptions != null) {
            return coreOptions.configFilename();   
        }
        return AstraConfiguration.getDefaultConfigurationFileName();
    }
    
    /**
     * Access output format
     * 
     * @return
     *      output format
     */
    public OutputFormat getOutputFormat() {
        if (coreOptions != null) {
            return coreOptions.output();   
        }
        return OutputFormat.human;
    }
    
    /**
     * No explicit token = We want to read one from section
     * @throws TokenNotFoundException
     *      tokken has not been found 
     */
    private void updateTokenWithSectionValue() 
    throws TokenNotFoundException {
        if (astraConfig.isSectionExists(tokenOptions.section()) &&
            astraConfig.getSection(tokenOptions.section())
                       .containsKey(ASTRA_DB_APPLICATION_TOKEN)) {
            LoggerShell.debug("configuration: Using token in section %s".formatted(tokenOptions.section()));
            this.tokenOptions = new TokenOptions(
                    astraConfig.getSection(tokenOptions.section())
                               .get(ASTRA_DB_APPLICATION_TOKEN), tokenOptions.section());
           
        } else {
            throw new TokenNotFoundException();
        }
    }
    
    /**
     * Invoke devops Api to check token
     */
    private void validateToken() {
        LoggerShell.debug("Token: " + getToken().substring(0, 20) + "...");
        if (!getToken().startsWith("AstraCS")) {
            LoggerShell.debug("Invalid Token");
            AstraCliConsole.outputError(ExitCode.INVALID_PARAMETER, "Token provided is invalid. It should start with 'AstraCS:...'. Try [astra setup]");
            throw new InvalidTokenException(getToken());
        }
        try {
            new OrganizationsClient(getToken()).organization();
            LoggerShell.info("Cli successfully initialized");
        } catch(Exception e) {
            AstraCliConsole.outputError(ExitCode.CANNOT_CONNECT, "Token provided is invalid. Try [astra setup]");
            throw new InvalidTokenException(getToken());
        }
    }
   
    /**
     * Getter accessor for attribute 'token'.
     *
     * @return
     *       current value of 'token'
     * @throws TokenNotFoundException
     *      token as not been found 
     */
    public String getToken() 
    throws TokenNotFoundException {
        if (StringUtils.isEmpty(tokenOptions.token())) {
            throw new TokenNotFoundException();
        }
        return tokenOptions.token();
    }

    /**
     * Getter accessor for attribute 'astraRc'.
     *
     * @return
     *       current value of 'astraRc'
     */
    public AstraConfiguration getConfiguration() {
        return astraConfig;
    }
    
    private DatabasesClient databasesClient;
    private StreamingClient streamingClient;
    private OrganizationsClient apiDevopsOrganizations;
    
    /**
     * Getter accessor for attribute 'apiDevopsDatabases'.
     *
     * @return
     *       current value of 'apiDevopsDatabases'
     */
    public DatabasesClient getApiDevopsDatabases() {
        if (databasesClient == null) {
            databasesClient = new DatabasesClient(getToken());
        }
        return databasesClient;
    }

    /**
     * Getter accessor for attribute 'apiDevopsStreaming'.
     *
     * @return
     *       current value of 'apiDevopsStreaming'
     */
    public StreamingClient getApiDevopsStreaming() {
        if (streamingClient == null) {
            streamingClient = new StreamingClient(getToken());
        }
        return streamingClient;
    }
    
    /**
     * Getter accessor for attribute 'apiDevopsOrganizations'.
     *
     * @return
     *       current value of 'apiDevopsOrganizations'
     */
    public OrganizationsClient getApiDevopsOrganizations() {
        if (apiDevopsOrganizations == null) {
            apiDevopsOrganizations = new OrganizationsClient(getToken());
        }
        return apiDevopsOrganizations;
    }
    

    /**
     * Getter accessor for attribute 'arguments'.
     *
     * @return
     *       current value of 'arguments'
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Setter accessor for attribute 'arguments'.
     * @param arguments
     * 		new value for 'arguments '
     */
    public void setArguments(List<String>  arguments) {
        this.arguments = arguments;
    }
  
}
