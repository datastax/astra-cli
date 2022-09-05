package com.datastax.astra.shell.cmd;

import com.datastax.astra.shell.ExitCode;
import com.github.rvesse.airline.annotations.Command;

/**
 * Unselect an entity (database)
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "quit", description = "Remove scope focus on an entity (prompt changed).")
public class QuitCmd extends BaseSh {

    /** {@inheritDoc} */
    @Override
    public ExitCode execute() {
        if (!dbSelected()) {
            return ExitCode.CONFLICT;
        }
        ctx().exitDatabase();
        return ExitCode.SUCCESS;
    }

}
