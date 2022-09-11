package com.datastax.astra.cli.db;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseCmd;
import com.datastax.astra.cli.core.exception.ParamValidationException;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show Databases for an organization 
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.LIST, description = "Display the list of Databases in an organization")
public class DbListCmd extends BaseCmd {
   
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        return OperationsDb.listDb();
    }

}
