package com.datastax.astra.cli.config;

import java.util.Scanner;

import org.fusesource.jansi.Ansi;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.sdk.organizations.OrganizationsClient;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

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
            AstraCliConsole.println("    _____            __                ",  Ansi.Color.YELLOW);
            AstraCliConsole.println("   /  _  \\   _______/  |_____________   ",  Ansi.Color.YELLOW);
            AstraCliConsole.println("  /  /_\\  \\ /  ___/\\   __\\_  __ \\__  \\ ",  Ansi.Color.YELLOW);
            AstraCliConsole.println(" /    |    \\\\___ \\  |  |  |  | \\// __ \\_",  Ansi.Color.YELLOW);
            AstraCliConsole.println(" \\____|__  /____  > |__|  |__|  (____  /",  Ansi.Color.YELLOW);
            AstraCliConsole.println("         \\/     \\/                   \\/\n",  Ansi.Color.YELLOW);
            
            try(Scanner scanner = new Scanner(System.in)) {
                boolean valid_token = false;
                while (!valid_token) {
                    AstraCliConsole.println("       ------------------------", Ansi.Color.CYAN);
                    AstraCliConsole.println("       ---       SETUP      ---", Ansi.Color.CYAN);
                    AstraCliConsole.println("       ------------------------\n", Ansi.Color.CYAN);
                    AstraCliConsole.println("🔑 Enter token (starting with AstraCS...):", Ansi.Color.YELLOW);
                    token = scanner.nextLine();
                    if (!token.startsWith("AstraCS:")) {
                        LoggerShell.error("Your token should start with 'AstraCS:'");
                    } else {
                        try {
                            createDefaultSection(token);
                            valid_token = true;
                        } catch(InvalidTokenException ite) {
                            // loop
                        }
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
            LoggerShell.error("Token provided is invalid. Please enter a valid token or quit with CTRL+C");
            throw new InvalidTokenException(token, e);
        }
    }
}
