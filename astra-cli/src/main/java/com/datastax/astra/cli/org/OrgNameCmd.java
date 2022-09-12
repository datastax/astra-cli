package com.datastax.astra.cli.org;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.BaseCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show organization name
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsOrganization.CMD_NAME, description = "Show organization name.")
public class OrgNameCmd extends BaseCmd {

    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationsOrganization.getName();
    }

}
