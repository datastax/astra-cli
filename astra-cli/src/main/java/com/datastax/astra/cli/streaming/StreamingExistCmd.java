package com.datastax.astra.cli.streaming;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display information relative to a tenant.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsStreaming.CMD_EXIST, description = "Show existence of a tenant")
public class StreamingExistCmd extends AbstractConnectedCmd {

    /** name of the DB. */
    @Required
    @Arguments(title = "TENANT", description = "Tenant name ")
    public String tenant;
    
    /** {@inheritDoc} */
    public void execute() {
        OperationsStreaming.showTenantExistence(tenant);
    }

}
