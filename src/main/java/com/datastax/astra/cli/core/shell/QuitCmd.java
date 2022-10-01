package com.datastax.astra.cli.core.shell;

import com.datastax.astra.cli.core.AbstractInteractiveCmd;
import com.datastax.astra.cli.db.exception.DatabaseNotSelectedException;
import com.github.rvesse.airline.annotations.Command;

/**
 * Unselect an entity (database)
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "quit", description = "Remove scope focus on an entity (prompt changed).")
public class QuitCmd extends AbstractInteractiveCmd {

    /** {@inheritDoc} */
    @Override
    public void execute() 
    throws DatabaseNotSelectedException {
        assertDbSelected();
        ctx().exitDatabase();
    }

}
