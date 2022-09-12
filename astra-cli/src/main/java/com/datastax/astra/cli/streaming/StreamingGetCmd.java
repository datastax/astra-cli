package com.datastax.astra.cli.streaming;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseCmd;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.GET, description = "Show details of a tenant")
public class StreamingGetCmd extends BaseCmd {

    /** Enum for db get. */
    public static enum StreamingGetKeys { 
        /* tenant status */
        status, 
        /* cloud provider*/
        cloud, 
        /* tpulsar token */
        pulsar_token,
        /* cloud region */
        region};
    
    /** name of the DB. */
    @Required
    @Arguments(title = "TENANT", description = "Tenant name ")
    public String tenant;
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-k", "--key" }, title = "Key", description = ""
            + "Show value for a property among: "
            + "'status', 'cloud', 'pulsar_token', 'region'")
    protected StreamingGetKeys key;
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws TenantNotFoundException {    
        return OperationsStreaming.showTenant(tenant, key);
    }

}
