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
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
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
    protected String token;

    /** To use Cli on non production environment. */
    @Option(name = { "-e", "--env" }, title = "Environment", description = "Environment to use for this section.")
    protected String env = "prod";

    protected String sectionName;

    /** {@inheritDoc} */
    @Override
    public void execute() {
        AstraEnvironment targetEnv = AstraCliUtils.parseEnvironment(env);
        // As not token is provided we ask for it in the console
        if (token == null || token.isBlank()) {
            verbose = true;
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
                    sectionName = new AstraOpsClient(token, targetEnv).getOrganization().getName();
                    validToken = true;
                } catch(InvalidTokenException ite) {
                    LoggerShell.error("Your token in invalid please retry " + ite.getMessage());
                }
            }
        }
        createDefaultSection();
    }
    
    /**
     * Based on provided token create the default section.
     * 
     * @throws InvalidTokenException
     *      invalid token provided 
     */
    private void createDefaultSection()
    throws InvalidTokenException {
        try {
            ConfigCreateCmd ccc = new ConfigCreateCmd();
            ccc.token       = removeQuotesIfAny(token);
            ccc.sectionName = sectionName;
            ccc.env         = env;
            ccc.run();
            AstraCliConsole.outputSuccess("Setup completed.");
            LoggerShell.info("Enter 'astra help' to list available commands.");
        } catch(Exception e) {
            LoggerShell.warning("Invalid token: Must be start with 'AstraCS:..' and have Organization Administrator privileges.");
            throw new InvalidTokenException(token, e);
        }
    }
}
