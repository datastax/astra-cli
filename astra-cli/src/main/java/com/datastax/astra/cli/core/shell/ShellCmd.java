package com.datastax.astra.cli.core.shell;

import java.util.Scanner;

import com.datastax.astra.cli.InteractiveShell;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.out.ShellPrinter;
import com.datastax.astra.cli.utils.CommandLineUtils;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * The is a COMMAND from the CLI when no command name is provided
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "shell", description = "Interactive mode (default if no command provided)")
public class ShellCmd extends AbstractConnectedCmd {
    
    /** Ask for version number. s*/
    @Option(name = { "--version" }, description = "Show version")
    protected boolean version = false;
    
    /** {@inheritDoc} */
    public void execute() throws Exception {
       // With version no need to init
       if (version) {
           ShellPrinter.outputData("version", ShellPrinter.version());
       } else {
           ShellContext.getInstance().init(this);
           LoggerShell.info("Shell : " + ShellContext.getInstance().getRawShellCommand());
           LoggerShell.info("Class : " + getClass().getName());
           ShellPrinter.banner();
           try(Scanner scanner = new Scanner(System.in)) {
               while(true) {
                   ShellPrinter.prompt();
                   String readline = scanner.nextLine();
                   ShellContext.getInstance().setRawShellCommand(readline);
                   if (null!= readline) {
                       // run AstraShell
                       InteractiveShell.main(CommandLineUtils.parseCommand(readline.trim()));
                   }
               }
           }
       }
    }

}
