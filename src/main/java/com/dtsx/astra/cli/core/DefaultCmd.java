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

import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.StringBuilderAnsi;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import org.fusesource.jansi.Ansi;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Question mark is a COMMAND from the CLI when no command name is provided
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "?", description = "Display this help version")
public class DefaultCmd extends AbstractCmd {
    
    /** Ask for version number. s*/
    @Option(name = { "--version" }, description = "Show version")
    protected boolean version = false;
    
    /** {@inheritDoc} */
    public void execute() {
        if (version) {
            AstraCliConsole.outputData("version", AstraCliUtils.version());
        } else {
            AstraCliConsole.banner();
            AstraCliConsole.println(new StringBuilderAnsi(" - Command list: ")
                    .append("astra help", Ansi.Color.CYAN));
            AstraCliConsole.println(new StringBuilderAnsi("️ - Get help: ")
                    .append("astra help <your command>", Ansi.Color.CYAN));
            AstraCliConsole.println("");
        }
    }    

}
