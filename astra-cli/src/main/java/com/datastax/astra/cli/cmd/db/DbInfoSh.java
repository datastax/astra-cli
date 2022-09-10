package com.datastax.astra.cli.cmd.db;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.cmd.BaseSh;
import com.datastax.astra.cli.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.exception.ParamValidationException;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "info", description = "Show details of a database (db must be selected)")
public class DbInfoSh extends BaseSh {

    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        if (!dbSelected()) {
            return ExitCode.CONFLICT;
        }
        return OperationsDb.showDb(ShellContext.getInstance().getDatabase().getId());
    }

}
