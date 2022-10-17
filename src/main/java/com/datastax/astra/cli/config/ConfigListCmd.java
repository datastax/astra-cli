package com.datastax.astra.cli.config;

import com.datastax.astra.cli.core.AbstractCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show the list of available configurations.
 * 
 * astra config list
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "list", description = "Show the list of available configurations.")
public class ConfigListCmd extends AbstractCmd {

    /** {@inheritDoc} */
    @Override
    public void execute() {
        OperationsConfig.listConfigurations();
    }
    
}
