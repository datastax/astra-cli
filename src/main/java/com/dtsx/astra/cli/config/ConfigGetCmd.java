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

import java.util.Optional;

import com.dtsx.astra.cli.core.AbstractCmd;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.exception.ConfigurationException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Allowing both syntax:
 * 
 * astra config show default
 * astra show config default 
 */
@Command(name = "get", description = "Show details for a configuration.")
public class ConfigGetCmd extends AbstractCmd {
    
    /**
     * Section in configuration file to as default.
     */
    @Required
    @Arguments(
       title = "sectionName", 
       description = "Section in configuration file to as as default.")
    protected String sectionName;
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-k", "--key" }, title = "Key in the section", description = "If provided return only value for a key.")
    protected String key;
    
    /** {@inheritDoc}  */
    @Override
    public void execute() throws ConfigurationException  {
        OperationsConfig.assertSectionExist(sectionName);
        if (key != null) {
            Optional<String> optKey = ctx().getConfiguration().getSectionKey(sectionName, key);
            if (optKey.isEmpty()) {
                AstraCliConsole.outputError(
                        ExitCode.INVALID_PARAMETER, 
                        "Key '" + key + "' has not been found in config section '" + sectionName + "'");
                throw new ConfigurationException("Key '" + key + "' has not been found in config section '" + sectionName + "'");
            } else {
                AstraCliConsole.println(optKey.get());
            }
        } else {
            AstraCliConsole.println(ctx().getConfiguration().renderSection(sectionName));
        }
     }
}
