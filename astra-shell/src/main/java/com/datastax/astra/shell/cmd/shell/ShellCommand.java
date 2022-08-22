package com.datastax.astra.shell.cmd.shell;

import java.util.Scanner;

import com.datastax.astra.shell.AstraShell;
import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.cmd.BaseCliCommand;
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
public class ShellCommand extends BaseCliCommand {
    
    /** 
     * Each command can have a verbose mode. 
     **/
    @Option(name = { "--version" }, description = "Show version")
    protected boolean version = false;
    
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
