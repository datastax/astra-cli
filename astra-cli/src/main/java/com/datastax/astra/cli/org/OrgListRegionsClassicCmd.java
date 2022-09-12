package com.datastax.astra.cli.org;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.BaseCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * List regions
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsOrganization.CMD_REGIONS, description = "Show available regions (classic).")
public class OrgListRegionsClassicCmd extends BaseCmd {

    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationsOrganization.listRegions();
    }

}
