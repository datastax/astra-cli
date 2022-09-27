package com.datastax.astra.cli.db.dsbulk;

import java.util.List;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.db.OperationsDb;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;

/**
 * This command allows to load data with DsBulk.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "dsbulk", description = "Load data leveraging DSBulk")
public class DbDSBulkCmd extends AbstractConnectedCmd {

    @Arguments(description = "Use DSBulk arguments")
    private List<String> dsbulkArguments;
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws Exception {
        if (dsbulkArguments.size() < 3) {
            throw new InvalidArgumentException("Please use format astra db dsbulk <DB_NAME> [dsbulk options]");
        }
        OperationsDb.runDsBulk(dsbulkArguments);
    }
    
}
