package com.datastax.astra.cli.config;

import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.out.ShellPrinter;
import com.datastax.astra.sdk.organizations.OrganizationsClient;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Setup the configuration
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "setup", description = "Initialize configuration file")
public class ConfigSetupCmd extends AbstractConfigCmd implements Runnable {
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-t", "--token" }, title = "AuthToken", description = "Key to use authenticate each call.")
    protected String tokenParam;
   
    /** {@inheritDoc} */
    @Override
    public void execute() throws Exception {
        // On setup you must have output
        if (StringUtils.isBlank(tokenParam)) {
            verbose = true;
            ShellPrinter.println("+-------------------------------+", Color.CYAN);
            ShellPrinter.println("+-     Astra CLI SETUP         -+", Color.CYAN);
            ShellPrinter.println("+-------------------------------+", Color.CYAN);
            System.out.println("\nWelcome to Astra Cli. We will guide you to start.");
            ShellPrinter.println("\n[Astra Setup]", Ansi.Color.CYAN);
            System.out.println("To use the cli you need to:");
            System.out.println(" • Create an Astra account on : https://astra.datastax.com");
            System.out.println(" • Create an Authentication token following: https://dtsx.io/create-astra-token");
            ShellPrinter.println("\n[Cli Setup]", Ansi.Color.CYAN);
            String token = null;
            
            try(Scanner scanner = new Scanner(System.in)) {
                boolean valid_token = false;
                while (!valid_token) {
                    ShellPrinter.println("\n• Enter your token (AstraCS:*):", Ansi.Color.YELLOW);
                    token = scanner.nextLine();
                    if (!token.startsWith("AstraCS:")) {
                        LoggerShell.error("Your token should start with 'AstraCS:'");
                    } else {
                        try {
                            createDefaultSection(token);
                            valid_token = true;
                        } catch(InvalidTokenException ite) {
                            // Not was not invalid, asking again
                        }
                    }
                }
            }
        } else {
            createDefaultSection(tokenParam);
        }
        ShellPrinter.println("\n[You are ready !] (configuration is stored in ~/.astrarc)", Ansi.Color.CYAN);
        System.out.println(" • 'astra db list' : list databases");
        System.out.println(" • 'astra db create demo --if-not-exist' : create new db");
        System.out.println(" • 'astra help' list available command groups");
        System.out.println(" • 'astra help <command>' :  help you on a particular command");
    }
    
    /**
     * Based on provided token create the default section.
     * 
     * @param token
     *      current token
     * @return
     *      if token is valid
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
            
            ConfigGetCmd configs    = new ConfigGetCmd();
            configs.sectionName     = ccc.sectionName;
            configs.run();
            
        } catch(Exception e) {
            LoggerShell.error("Token provided is invalid. Please enter a valid token or quit with CTRL+C");
            throw new InvalidTokenException(token, e);
        }
    }
    
    /**
     * Update property.
     * 
     * @param t
     *      current token
     * @return
     *      current reference
     */
    public ConfigSetupCmd token(String t) {
        this.tokenParam = t;
        return this;
    }
}
