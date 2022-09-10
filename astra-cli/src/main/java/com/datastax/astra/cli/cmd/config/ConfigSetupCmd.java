package com.datastax.astra.cli.cmd.config;

import java.util.Scanner;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

import com.datastax.astra.cli.out.LoggerShell;
import com.datastax.astra.cli.out.ShellPrinter;
import com.datastax.astra.sdk.organizations.OrganizationsClient;
import com.github.rvesse.airline.annotations.Command;

/**
 * Setup the configuration
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "setup", description = "Initialize configuration file")
public class ConfigSetupCmd extends BaseConfigCommand implements Runnable {
    
    /** {@inheritDoc} */
    @Override
    public void run() {
        // On setup you must have output
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
        System.out.println("You will be asked to enter your token, it will be saved locally in ~/.astrarc");  
        String token = null;
        try(Scanner scanner = new Scanner(System.in)) {
            boolean valid_token = false;
            while (!valid_token) {
                ShellPrinter.println("\n• Enter your token (starting with AstraCS) : ", Ansi.Color.YELLOW);
                token = scanner.nextLine();
                if (!token.startsWith("AstraCS:")) {
                    LoggerShell.error("Your token should start with 'AstraCS:'");
                } else {
                    try {
                        ConfigCreateCmd ccc = new ConfigCreateCmd();
                        ccc.token = token;
                        ccc.sectionName =  new OrganizationsClient(token).organization().getName();
                        ccc.run();
                        
                        valid_token = true;
                        
                        ConfigGetCmd configs      = new ConfigGetCmd();
                        configs.astraRc         = this.astraRc;
                        configs.configFilename  = this.configFilename;
                        configs.sectionName     = ccc.sectionName;
                        configs.run();
                        
                    } catch(Exception e) {
                        LoggerShell.error("Token provided is invalid. Please enter a valid token or quit with CTRL+C");
                        e.printStackTrace();
                    }
                }
            }
            ShellPrinter.println("\n[What's NEXT ?]", Ansi.Color.CYAN);
            System.out.println("You are all set.(configuration is stored in ~/.astrarc) You can now:");
            System.out.println("   • Use any command, 'astra help' will get you the list");
            System.out.println("   • Try with 'astra db list'");
            System.out.println("   • Enter interactive mode using 'astra'");
            System.out.println("\nHappy Coding !");
            System.out.println("");
        }
    }

}
