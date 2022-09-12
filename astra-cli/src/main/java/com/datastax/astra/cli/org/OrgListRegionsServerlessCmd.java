package com.datastax.astra.cli.org;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.BaseCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * List regions serverless.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsOrganization.CMD_SERVERLESS, description = "Show available regions (serverless).")
public class OrgListRegionsServerlessCmd extends BaseCmd {

    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationsOrganization.listRegionsServerless();
    }

}
