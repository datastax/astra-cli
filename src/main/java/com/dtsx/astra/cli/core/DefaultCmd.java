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
import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.model.GlobalMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.dtsx.astra.cli.core.out.AstraAnsiColors.BLUE_300;
import static com.github.rvesse.airline.help.Help.help;

/**
 * Question mark is a COMMAND from the CLI when no command name is provided.
 */
@Command(name = "?", description = "Display this help version")
public class DefaultCmd extends AbstractCmd {

    @AirlineModule
    public GlobalMetadata<?> global;

    /** Ask for version number. s*/
    @Option(name = { "--version" }, description = "Show version")
    protected boolean version = false;
    
    /** {@inheritDoc} */
    public void execute() {
        if (version) {
            AstraCliConsole.outputData("version", AstraCliUtils.version());
        } else {
            AstraCliConsole.banner();
            AstraCliConsole.println(new StringBuilderAnsi("️Documentation : ")
                    .append("https://awesome-astra.github.io/docs/pages/astra/astra-cli/\n", BLUE_300));
            try {
                Help.help(global, new ArrayList<>());
            } catch(Exception e) {
                AstraCliConsole.println("Error while displaying help");
            }
            AstraCliConsole.println("\nSample commands : ");
            AstraCliConsole.println(new StringBuilderAnsi("️    List databases         : ")
                    .append("astra db list", BLUE_300));
            AstraCliConsole.println(new StringBuilderAnsi("️    Create vector database : ")
                    .append("astra db create demo --vector", BLUE_300));
            AstraCliConsole.println(new StringBuilderAnsi("️    List collections       : ")
                    .append("astra db list-collections demo", BLUE_300));
        }
    }    

}
