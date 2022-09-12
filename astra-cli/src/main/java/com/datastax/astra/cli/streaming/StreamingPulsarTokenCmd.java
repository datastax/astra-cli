package com.datastax.astra.cli.streaming;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.BaseCmd;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display information relative to a tenant.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsStreaming.CMD_GET_TOKEN, description = "Show status of a tenant")
public class StreamingPulsarTokenCmd extends BaseCmd {

    /** name of the DB. */
    @Required
    @Arguments(title = "TENANT", description = "Tenant name ")
    public String tenant;
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws  TenantNotFoundException {
        return OperationsStreaming.showTenantPulsarToken(tenant);
    }

}
