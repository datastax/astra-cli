package com.datastax.astra.cli.cmd.db.keyspace;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.cmd.BaseSh;
import com.datastax.astra.cli.cmd.db.OperationsDb;
import com.datastax.astra.cli.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.exception.ParamValidationException;
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
