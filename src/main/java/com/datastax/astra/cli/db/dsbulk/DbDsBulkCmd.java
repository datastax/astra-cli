package com.datastax.astra.cli.db.dsbulk;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;

import java.util.List;

/**
 * DSBulk generic command.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "dsbulk", description = "Count items for a table, a query")
public class DbDsBulkCmd extends AbstractConnectedCmd {

    @Arguments(description = "Provide as many dsbulk parameters as you want.")
    private List<String> dsbulkArguments;

    /** {@inheritDoc} */
    @Override
    protected void execute() {
        DsBulkService.getInstance().runRaw(dsbulkArguments);
    }
}
