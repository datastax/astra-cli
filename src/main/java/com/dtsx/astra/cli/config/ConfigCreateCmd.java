package com.dtsx.astra.cli.config;

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

import com.dtsx.astra.cli.core.AbstractCmd;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.exception.InvalidTokenException;
import com.dtsx.astra.cli.core.exception.TokenNotFoundException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.sdk.org.OrganizationsClient;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Create a new section in configuration.
 * 
 * "astra config create"
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "create", description = "Create a new section in configuration")
public class ConfigCreateCmd extends AbstractCmd {
    
    /** Section in configuration file to as default. */
    @Required
    @Arguments(title = "sectionName", description = "Section in configuration file to as as default.")
    protected String sectionName;
   
    /** Authentication token used if not provided in config. */
    @Option(name = { "-t", "--token" }, title = "AuthToken", description = "Key to use authenticate each call.")
    protected String token;
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (token == null) {
            AstraCliConsole.outputError(ExitCode.INVALID_PARAMETER, "Please Provide a token with option -t, --token");
            throw new TokenNotFoundException();
        }
        if (!token.startsWith("AstraCS:")) {
            AstraCliConsole.outputError(ExitCode.INVALID_PARAMETER, "Your token should start with 'AstraCS:'");
            throw new InvalidTokenException(token);
        }
        OrganizationsClient apiOrg  = new OrganizationsClient(token);
        Organization o = apiOrg.organization();
        if (sectionName == null) {
            sectionName = o.getName();
        }
        ctx().getConfiguration().createSectionWithToken(sectionName, token);
        ctx().getConfiguration().save();
        AstraCliConsole.outputSuccess("Configuration has been saved.");
    }
    
}
