package com.datastax.astra.cli.streaming;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a tenant if exists
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "delete", description = "Delete an existing tenant")
public class StreamingDeleteCmd extends AbstractConnectedCmd {
    
    /** name of the DB. */
    @Required
    @Arguments(title = "TENANT", description = "Tenant name ")
    public String tenant;
    
    /** {@inheritDoc} */
    public void execute()
    throws TenantNotFoundException {
        OperationsStreaming.deleteTenant(tenant);
    }
    
}
