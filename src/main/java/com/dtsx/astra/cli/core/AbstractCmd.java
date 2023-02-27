/*-
 * ----------LICENSE----------
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
 * ----------------
 */
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

import com.dtsx.astra.cli.config.AstraConfiguration;
import com.dtsx.astra.cli.core.out.AstraColorScheme;
import com.dtsx.astra.cli.core.out.OutputFormat;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.parser.errors.ParseRestrictionViolatedException;

import java.util.Arrays;
import java.util.List;

/**
 * Options, parameters and treatments that you want to apply on all commands.
 */
public abstract class AbstractCmd implements Runnable, AstraColorScheme {
    
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
    protected String output = OutputFormat.HUMAN.name();
     
    /**
     * File on disk to reuse configuration.
     */
    @Option(name = { "-cf", "--config-file" }, 
            title = "CONFIG_FILE",
            description= "Configuration file (default = ~/.astrarc)")
    protected String configFilename = AstraConfiguration.getDefaultConfigurationFileName();
    
    /** {@inheritDoc} */
    public void run() {
        validateOptions();
        ctx().init(new CoreOptions(verbose, noColor, OutputFormat.valueOf(output.toUpperCase()), configFilename));
        execute();
    }

    /**
     * Check parameters and throws specialized error
     */
    protected void validateOptions() {
        List<String> validFormats = Arrays.stream(OutputFormat.values()).map(OutputFormat::name).toList();
        if (!validFormats.contains(output.toUpperCase())) {
            throw new ParseRestrictionViolatedException("Invalid option value (-o, --output), expecting human,json or csv");
        }
    }
    
    /**
     * Function to be implemented by terminal class.
     */
    protected abstract void execute();
    
    /**
     * Get current context.
     * 
     * @return
     *      current context
     */
    protected CliContext ctx() {
        return CliContext.getInstance();
    }
}
