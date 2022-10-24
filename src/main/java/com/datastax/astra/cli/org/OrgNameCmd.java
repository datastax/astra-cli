package com.datastax.astra.cli.org;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show organization name
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OrganizationService.CMD_NAME, description = "Show organization name.")
public class OrgNameCmd extends AbstractConnectedCmd {

    /** {@inheritDoc} */
    public void execute() {
        OrganizationService.getInstance().getName();
    }

}
