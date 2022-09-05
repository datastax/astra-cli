package com.datastax.astra.shell.cmd.shell;

import java.util.Scanner;

import com.datastax.astra.shell.AstraShell;
import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.cmd.BaseCmd;
import com.datastax.astra.shell.out.LoggerShell;
import com.datastax.astra.shell.out.ShellPrinter;
import com.datastax.astra.shell.utils.CommandLineUtils;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * The is a COMMAND from the CLI when no command name is provided
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "shell", description = "Interactive mode (default if no command provided)")
public class ShellCmd extends BaseCmd {
    
    /** 
     * Each command can have a verbose mode. 
     **/
    @Option(name = { "--version" }, description = "Show version")
    protected boolean version = false;
    
    /** {@inheritDoc} */
    @Override
    public void run() {
       // for the command --version configuration is NOT mandatory
       if (!ctx().isInitialized() && !version) {
           ShellPrinter.outputError(ExitCode.CONFLICT, "A shell command should have the connection set");
       } else {
           ctx().setCurrentShellCommand(this);
           LoggerShell.info("Shell : " + ShellContext.getInstance().getRawShellCommand());
           LoggerShell.info("Class : " + getClass().getName());
           execute();
       }
    }
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        
        if (version) {
            ShellPrinter.outputData("version", ShellPrinter.version());
            ExitCode.SUCCESS.exit();
        }
        
        // Show Banner
        ShellPrinter.banner();
        
        // Interactive mode
        try(Scanner scanner = new Scanner(System.in)) {
            while(true) {
                ShellPrinter.prompt();
                String readline = scanner.nextLine();
                
                // Save User input as shell command
                ShellContext.getInstance().setRawShellCommand(readline);
                
                if (null!= readline) {
                    AstraShell.main(CommandLineUtils.parseCommand(readline.trim()));
                }
            }
        }
       
    }

}
