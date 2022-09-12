package com.datastax.astra.cli.org;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.GET, description = "Show details of an organization")
public class OrgGetCmd extends BaseCmd {

    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationsOrganization.showOrg();
    }

}
