package com.datastax.astra.cli.db;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.BaseSh;
import com.datastax.astra.cli.core.exception.ParamValidationException;
import com.datastax.astra.cli.db.DbGetCmd.DbGetKeys;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "info", description = "Show details of a database (db must be selected)")
public class DbInfoSh extends BaseSh {

    /** Authentication token used if not provided in config. */
    @Option(name = { "-k", "--key" }, title = "Key", description = ""
            + "Show value for a property among: "
            + "'id', 'status', 'cloud', 'keyspace', 'keyspaces', 'region', 'regions'")
    protected DbGetKeys key;
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        if (!dbSelected()) {
            return ExitCode.CONFLICT;
        }
        return OperationsDb.showDb(ShellContext.getInstance().getDatabase().getId(), key);
    }

}
