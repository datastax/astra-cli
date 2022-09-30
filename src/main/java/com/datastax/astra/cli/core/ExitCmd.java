package com.datastax.astra.cli.core;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.out.ShellPrinter;
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
    public void execute() throws Exception {
        ShellPrinter.outputSuccess("Exiting Astra Cli");
        System.exit(ExitCode.SUCCESS.getCode());
    }

}
