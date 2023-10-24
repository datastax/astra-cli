package com.dtsx.astra.cli.config;

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

import com.dtsx.astra.cli.core.AbstractCmd;
import com.dtsx.astra.cli.core.exception.InvalidTokenException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

import static com.dtsx.astra.cli.core.out.AstraAnsiColors.CYAN_400;

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
            boolean validToken = false;
            Console cons;
            char[] tokenFromConsole;
            while (!validToken) {
                try {
                    if ((cons = System.console()) != null &&
                            (tokenFromConsole = cons.readPassword("[%s]", "$ Enter an Astra token:")) != null) {
                        token = String.valueOf(tokenFromConsole);

                        // Clear the password from memory immediately when done
                        Arrays.fill(tokenFromConsole, ' ');
                    } else {
                        try (Scanner scanner = new Scanner(System.in)) {
                            AstraCliConsole.println("$ Enter an Astra token:", CYAN_400);
                            token = scanner.nextLine();
                        }
                    }
                    createDefaultSection(removeQuotesIfAny(token));
                    validToken = true;
                } catch(InvalidTokenException ite) {
                    LoggerShell.error("Your token in invalid please retry " + ite.getMessage());
                }
            }
        } else {
            createDefaultSection(tokenParam);
        }
        LoggerShell.info("Enter 'astra help' to list available commands.");
        AstraCliConsole.outputSuccess("Setup completed.");
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
            ccc.token = removeQuotesIfAny(token);
            ccc.sectionName = new AstraOpsClient(token).getOrganization().getName();
            ccc.run();
        } catch(Exception e) {
            LoggerShell.warning("Invalid token: It must be start with 'AstraCS:..' and have Organization Administrator privileges.");
            LoggerShell.warning("Generated token at database creation cannot be used.");
            LoggerShell.warning("Please enter a valid token or quit with CTRL+C.");
            throw new InvalidTokenException(token, e);
        }
    }
}
