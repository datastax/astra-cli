package com.datastax.astra.cli.core.shell;

import java.util.Optional;

import com.datastax.astra.cli.core.AbstractInteractiveCmd;
import com.datastax.astra.cli.core.exception.ConfigurationException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.sdk.config.AstraClientConfig;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Connection to another organization.
 * 
 * Should be replace by config load.
 *
 * connect --org mdddd
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "connect", description = "Connect to another Astra Organization")
public class ConnectSh extends AbstractInteractiveCmd {
    
    /**
     * Section name in configuration file.
     */
    @Required
    @Arguments(title = "configName", description = "Section name in configuration file")
    public String configName;
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws Exception {
        if (!ctx().getAstraRc().isSectionExists(configName)) {
            throw new ConfigurationException("Config '" + configName + "' has not been found in configuration file.");
        } else {
            Optional<String> newToken = ctx()
                    .getAstraRc()
                    .getSectionKey(configName, AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN);
            if (newToken.isPresent()) {
                ctx().connect(newToken.get());
            } else {
                LoggerShell.error("Token not found for '" + configName + "'");
            }
        }
    }
}
