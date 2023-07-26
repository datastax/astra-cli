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

import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.StringBuilderAnsi;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

import static com.dtsx.astra.cli.core.out.AstraAnsiColors.BLUE_300;
/**
 * Question mark is a COMMAND from the CLI when no command name is provided.
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
            AstraCliConsole.println(new StringBuilderAnsi(" - list commands       : ")
                    .append("astra help", BLUE_300));
            AstraCliConsole.println(new StringBuilderAnsi("️ - get command help    : ")
                    .append("astra help <your command>", BLUE_300));
            AstraCliConsole.println(new StringBuilderAnsi("️ - list your databases : ")
                    .append("astra db list", BLUE_300));
            AstraCliConsole.println(new StringBuilderAnsi("️ - create new database : ")
                    .append("astra db create demo\n", BLUE_300));
        }
    }    

}
