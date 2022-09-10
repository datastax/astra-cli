package com.datastax.astra.cli.cmd.streaming.pulsarshell;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.cmd.BaseCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * This command allows to load data with pulsar-client.
 * 
 * 
 * astra pulsar-shell
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "pulsar-shell", description = "Start pulsar admin against your tenant")
public class PulsarShellCmd extends BaseCmd {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "TENANT", description = "Tenant unique name")
    public String tenant;
    
    /** {@inheritDoc} */
    @Override
    public ExitCode execute() {
        System.out.println("This feature is in developement");
        // TODO
        // Download pulsar-shell if not present
        // Tenant name is required as argument
        // - get info from devops API to create a client.conf
        // - create client.conf
        // - launch pulsar-shell
        return ExitCode.SUCCESS;
    }
    
}
