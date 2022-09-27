package com.datastax.astra.cli.org;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * List regions serverless.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsOrganization.CMD_SERVERLESS, description = "Show available regions (serverless).")
public class OrgListRegionsServerlessCmd extends AbstractConnectedCmd {

    /** {@inheritDoc} */
    public void execute() {
        OperationsOrganization.listRegionsServerless();
    }

}
