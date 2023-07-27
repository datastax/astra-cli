package com.dtsx.astra.cli.core;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.config.AstraCliConfiguration;
import com.dtsx.astra.cli.core.exception.AstraEnvironmentNotFoundException;
import com.dtsx.astra.cli.core.exception.InvalidTokenException;
import com.dtsx.astra.cli.core.exception.TokenNotFoundException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.core.out.OutputFormat;
import com.dtsx.astra.sdk.AstraDevopsApiClient;
import com.dtsx.astra.sdk.db.AstraDbClient;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.streaming.AstraStreamingClient;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraRc;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Hold the context of CLI to know where we are.
 * The singleton pattern is validated with a Lazy initialization
 * and a thread safe implementation.
 */
@SuppressWarnings("java:S6548")
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
    private AstraCliConfiguration astraConfig;
     
    /**
     * Should initialize the client based on provided parameters.
     *
     * @param options
     *      options of the cli
     */
    public void init(CoreOptions options)  {
        this.coreOptions = options;
        this.astraConfig = new AstraCliConfiguration(coreOptions.configFilename());
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
            loadCredentialsFromSection();
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
    private void loadCredentialsFromSection()
    throws TokenNotFoundException {
        if (astraConfig.isSectionExists(tokenOptions.section()) &&
            astraConfig.getSection(tokenOptions.section())
                       .containsKey(AstraRc.ASTRA_DB_APPLICATION_TOKEN)) {
            LoggerShell.debug("Configuration: Using token in section %s".formatted(tokenOptions.section()));
            ApiLocator.AstraEnvironment targetEnv = ApiLocator.AstraEnvironment.PROD;
            if (astraConfig.getSection(tokenOptions.section()).containsKey(AstraCliConfiguration.KEY_ENV)) {
                targetEnv = ApiLocator.AstraEnvironment.valueOf(
                        astraConfig.getSection(tokenOptions.section()).get(AstraCliConfiguration.KEY_ENV));
                LoggerShell.debug("Configuration: Targeting env %s".formatted(targetEnv));
            }
            this.tokenOptions = new TokenOptions(
                    astraConfig.getSection(tokenOptions.section())
                               .get(AstraRc.ASTRA_DB_APPLICATION_TOKEN),
                                tokenOptions.section(), targetEnv);
           
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
        getApiDevops();
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
     * Access the target environment for Astra Platform.
     *
     * @return
     *       current value of 'astra environment'
     *       current value of 'astra environment'
     * @throws TokenNotFoundException
     *      token as not been found
     */
    public ApiLocator.AstraEnvironment getAstraEnvironment()
    throws TokenNotFoundException {
        if (tokenOptions == null || tokenOptions.env() == null) {
            throw new AstraEnvironmentNotFoundException();
        }
        return tokenOptions.env();
    }

    /**
     * Getter accessor for attribute 'astraRc'.
     *
     * @return
     *       current value of 'astraRc'
     */
    public AstraCliConfiguration getConfiguration() {
        return astraConfig;
    }

    private AstraDevopsApiClient devopsApiClient;

    /**
     * Getter accessor for attribute 'apiDevopsDatabases'.
     *
     * @return
     *       current value of 'apiDevopsDatabases'
     */
    public AstraDevopsApiClient getApiDevops() {
        if (devopsApiClient == null) {
            devopsApiClient = new AstraDevopsApiClient(getToken(), getAstraEnvironment());
            if (!getAstraEnvironment().equals(ApiLocator.AstraEnvironment.PROD)) {
                LoggerShell.info("You are using a non-production environment '%s' ".formatted(getAstraEnvironment()));
            }
            validateDevopsClientConnection(devopsApiClient);
        }
        return devopsApiClient;
    }

    /**
     * Validate a token for a target environment.
     *
     * @param token
     *      current token
     * @param env
     *      target environment
     */
    public void validateCredentials(String token, ApiLocator.AstraEnvironment env) {
        validateDevopsClientConnection(new AstraDevopsApiClient(token, env));
    }

    /**
     * Validate that current Api Client is valid.
     */
    private void validateDevopsClientConnection(AstraDevopsApiClient client) {
        Organization org = client.getOrganization();
        if (org.getId() == null) {
            if (!client.getEnvironment().equals(ApiLocator.AstraEnvironment.PROD)) {
                AstraCliConsole.outputError(ExitCode.CANNOT_CONNECT,
                        "Make sure token targets proper environment '%s'".formatted(client.getEnvironment()));
            }
            throw new InvalidTokenException(getToken());
        }
    }

    /**
     * Getter accessor for attribute 'apiDevopsDatabases'.
     *
     * @return
     *       current value of 'apiDevopsDatabases'
     */
    public AstraDbClient getApiDevopsDatabases() {
        AstraDevopsApiClient cli = getApiDevops();
        AstraDbClient dbCli =  cli.db();;
        return dbCli;

    }

    /**
     * Getter accessor for attribute 'apiDevopsStreaming'.
     *
     * @return
     *       current value of 'apiDevopsStreaming'
     */
    public AstraStreamingClient getApiDevopsStreaming() {
        return getApiDevops().streaming();
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
