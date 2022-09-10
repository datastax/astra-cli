package com.datastax.astra.cli.cmd.db.dsbulk;

import java.util.List;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.cmd.BaseCmd;
import com.datastax.astra.cli.cmd.db.OperationsDb;
import com.datastax.astra.cli.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.exception.ParamValidationException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;

/**
 * This command allows to load data with DsBulk.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "dsbulk", description = "Load data leveraging DSBulk")
public class DbDSBulkCmd extends BaseCmd {

    @Arguments(description = "Use DSBulk arguments")
    private List<String> dsbulkArguments;
    
    /** {@inheritDoc} */
    @Override
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        if (dsbulkArguments.size() < 3) {
            throw new IllegalArgumentException("Please use format astra db dsbulk <DB_NAME> [dsbulk options]");
        }
        return OperationsDb.runDsBulk(dsbulkArguments);
    }
    
}
