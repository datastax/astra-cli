package com.datastax.astra.cli.org;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.BaseCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show organization id
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsOrganization.CMD_ID, description = "Show organization id.")
public class OrgIdCmd extends BaseCmd {

    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationsOrganization.getId();
    }

}
