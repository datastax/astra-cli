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
import com.dtsx.astra.cli.core.out.OutputFormat;
import com.github.rvesse.airline.annotations.Option;

import java.util.Locale;

/**
 * Base command for cli. The cli have to deal with configuration file and initialize connection
 * each item where shell already have the context initialized.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractConnectedCmd extends AbstractCmd {
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "--token" }, 
            title = "AUTH_TOKEN",
            description = "Key to use authenticate each call.")
    protected String token;

    /** Section. */
    @Option(name = { "-conf","--config" }, 
            title = "CONFIG_SECTION",
            description= "Section in configuration file (default = ~/.astrarc)")
    protected String configSectionName = AstraConfiguration.ASTRARC_DEFAULT;

    /** {@inheritDoc} */
    @Override
    public void run() {
        validateOptions();
        ctx().init(new CoreOptions(verbose, noColor, OutputFormat.valueOf(output.toUpperCase(Locale.ROOT)), configFilename));
        ctx().initToken(new TokenOptions(token, configSectionName));
        execute();
    }

}
