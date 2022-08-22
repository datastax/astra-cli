package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.cmd.BaseShellCommand;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "info", description = "Show details of a database (db must be selected)")
public class DbInfoShell extends BaseShellCommand {

    /** {@inheritDoc} */
    public ExitCode execute() {
        if (!dbSelected()) {
            return ExitCode.CONFLICT;
        }
        return OperationsDb.showDb(ShellContext.getInstance().getDatabase().getId());
    }

}
