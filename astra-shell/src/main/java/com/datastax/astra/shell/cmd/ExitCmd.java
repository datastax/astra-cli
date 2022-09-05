package com.datastax.astra.shell.cmd;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.out.ShellPrinter;
import com.github.rvesse.airline.annotations.Command;

/**
 * Exit properly from the Shell.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "exit", description = "Exit program.")
public class ExitCmd extends BaseSh {

   /** {@inheritDoc} */
    @Override
    public ExitCode execute() {
       ShellPrinter.outputSuccess("Exiting Astra Cli");
       ExitCode.SUCCESS.exit();
       // Nerver reachede
       return ExitCode.SUCCESS;
    }

}
