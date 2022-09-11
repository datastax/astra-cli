package com.datastax.astra.cli.db;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseCmd;
import com.datastax.astra.cli.core.exception.ParamValidationException;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.GET, description = "Show details of a database")
public class DbGetCmd extends BaseCmd {

    /** Enum for db get. */
    public static enum DbGetKeys { id, status, cloud, keyspace, keyspaces, region, regions};
    
    /** name of the DB. */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String database;
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-k", "--key" }, title = "Key", description = ""
            + "Show value for a property among: "
            + "'id', 'status', 'cloud', 'keyspace', 'keyspaces', 'region', 'regions'")
    protected DbGetKeys key;
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {    
        return OperationsDb.showDb(database, key);
    }

}
