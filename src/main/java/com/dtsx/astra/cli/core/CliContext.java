package com.dtsx.astra.cli.core;

/*-
 * #%L
 * Astra Cli
 * %%
 * Copyright (C) 2022 DataStax
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.config.AstraConfiguration;
import com.dtsx.astra.cli.core.exception.InvalidTokenException;
import com.dtsx.astra.cli.core.exception.TokenNotFoundException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.core.out.OutputFormat;
import com.dtsx.astra.sdk.db.DatabasesClient;
import com.dtsx.astra.sdk.org.OrganizationsClient;
import com.dtsx.astra.sdk.streaming.StreamingClient;
import com.dtsx.astra.sdk.utils.AstraRc;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Hold the context of CLI to know where we are.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class CliContext {

    /**
     * Singleton Pattern, private instance.
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
     * Should initialize the client based on provided parameters.
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
     * Access output format
     * 
     * @return
     *      output format
     */
    public OutputFormat getOutputFormat() {
        if (coreOptions != null) {
            return coreOptions.output();   
        }
        return OutputFormat.HUMAN;
    }
    
    /**
     * No explicit token = We want to read one from section
     * @throws TokenNotFoundException
     *      token has not been found
     */
    private void updateTokenWithSectionValue() 
    throws TokenNotFoundException {
        if (astraConfig.isSectionExists(tokenOptions.section()) &&
            astraConfig.getSection(tokenOptions.section())
                       .containsKey(AstraRc.ASTRA_DB_APPLICATION_TOKEN)) {
            LoggerShell.debug("configuration: Using token in section %s".formatted(tokenOptions.section()));
            this.tokenOptions = new TokenOptions(
                    astraConfig.getSection(tokenOptions.section())
                               .get(AstraRc.ASTRA_DB_APPLICATION_TOKEN), tokenOptions.section());
           
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
            LoggerShell.debug("Cli successfully initialized");
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
        if (tokenOptions == null || StringUtils.isEmpty(tokenOptions.token())) {
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
