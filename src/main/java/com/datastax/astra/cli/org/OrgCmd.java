package com.datastax.astra.cli.org;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "get", description = "Show details of an organization")
public class OrgCmd extends AbstractConnectedCmd {

    /** {@inheritDoc} */
    public void execute() {
        OperationsOrganization.showOrg();
    }

}
