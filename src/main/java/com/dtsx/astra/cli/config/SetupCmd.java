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
import com.dtsx.astra.cli.core.exception.InvalidTokenException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.sdk.org.OrganizationsClient;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.fusesource.jansi.Ansi;

import java.util.Scanner;

/**
 * Set up the configuration
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "setup", description = "Initialize configuration file")
public class SetupCmd extends AbstractCmd {
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-t", "--token" }, 
            title = "TOKEN", 
            description = "Key to use authenticate each call.")
    protected String tokenParam;
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (tokenParam == null || tokenParam.isBlank()) {
            verbose = true;
            String token;
            AstraCliConsole.banner();
            try(Scanner scanner = new Scanner(System.in)) {
                boolean valid_token = false;
                while (!valid_token) {
                    AstraCliConsole.println(" -----------------------", Ansi.Color.CYAN);
                    AstraCliConsole.println(" ---      SETUP      ---", Ansi.Color.CYAN);
                    AstraCliConsole.println(" -----------------------\n",Ansi.Color.CYAN);
                    AstraCliConsole.println("$ Enter an Astra token:", Ansi.Color.CYAN);
                    token = scanner.nextLine();
                    if (!token.startsWith("AstraCS:")) {
                        LoggerShell.error("Your token should start with 'AstraCS:'");
                    } else {
                        try {
                            createDefaultSection(token);
                            valid_token = true;
                        } catch(InvalidTokenException ite) {}
                    }
                }
            }
        } else {
            createDefaultSection(tokenParam);
        }
        AstraCliConsole.outputSuccess(" Enter 'astra help' to list available commands.");
    }
    
    /**
     * Based on provided token create the default section.
     * 
     * @param token
     *      token to create a section
     * @throws InvalidTokenException
     *      invalid token provided 
     */
    private void createDefaultSection(String token) 
    throws InvalidTokenException {
        try {
            ConfigCreateCmd ccc = new ConfigCreateCmd();
            ccc.token = token;
            ccc.sectionName = new OrganizationsClient(token).organization().getName();
            ccc.run();
        } catch(Exception e) {
            LoggerShell.warning("Invalid token: It must be start with 'AstraCS:..' and have Organization Administrator privileges.");
            LoggerShell.warning("Generated token at database creation cannot be used.");
            LoggerShell.warning("Please enter a valid token or quit with CTRL+C.");
            throw new InvalidTokenException(token, e);
        }
    }
}
