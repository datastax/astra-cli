package com.datastax.astra;

import org.fusesource.jansi.Ansi;

import com.datastax.astra.cli.AstraCli;
import com.datastax.astra.cli.core.out.ShellPrinter;

/**
 * Super class for tests.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractAstraCliTest {

    /**
     * Helper to execute a command
     * 
     * @param args
     *      args as providede in the command line
     */
    protected void astraCli(String... args) {
        System.out.println("----- TESTED COMMAND ------");
        ShellPrinter.print(" astra ", Ansi.Color.GREEN);
        for (String string : args) { 
            ShellPrinter.print(string + " ", Ansi.Color.GREEN);
        }
        System.out.println();
        System.out.println("---------------------------");
        System.out.println();
        AstraCli.main(args);
    }
    
}
