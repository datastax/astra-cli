package com.datastax.astra.cli.streaming;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display information relative to a tenant.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsStreaming.CMD_STATUS, description = "Show status of a tenant")
public class StreamingStatusCmd extends AbstractConnectedCmd {

    /** name of the DB. */
    @Required
    @Arguments(title = "TENANT", description = "Tenant name ")
    public String tenant;
    
    /** {@inheritDoc} */
    public void execute()
    throws TenantNotFoundException {
        OperationsStreaming.showTenantStatus(tenant);
    }

}
