package com.datastax.astra.shell.cmd.db.keyspace;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.cmd.BaseSh;
import com.datastax.astra.shell.cmd.db.OperationsDb;
import com.datastax.astra.shell.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.shell.exception.DatabaseNotFoundException;
import com.datastax.astra.shell.exception.ParamValidationException;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show Keyspaces for an Database.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsDb.CMD_LIST_KEYSPACES, description = "Display the list of Keyspaces in an database")
public class DbListKeyspacesSh extends BaseSh {
  
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        if (!dbSelected()) {
            return ExitCode.CONFLICT;
        }
        return OperationsDb.listKeyspaces(ShellContext.getInstance().getDatabase().getId());
    }

}
