package com.datastax.astra.shell.cmd.db;

import java.util.List;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCmd;
import com.datastax.astra.shell.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.shell.exception.DatabaseNotFoundException;
import com.datastax.astra.shell.exception.ParamValidationException;
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
