package com.datastax.astra.cli.cmd.shell;

import java.util.Optional;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.cmd.BaseSh;
import com.datastax.astra.cli.out.LoggerShell;
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
public class ConnectSh extends BaseSh {
    
    /**
     * Section name in configuration file.
     */
    @Required
    @Arguments(title = "configName", description = "Section name in configuration file")
    public String configName;
    
    /** {@inheritDoc} */
    @Override
    public ExitCode execute() {
        if (!ctx().getAstraRc().isSectionExists(configName)) {
            LoggerShell.error("Config '" + configName + "' has not been found in configuration file.");
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
        return ExitCode.SUCCESS;
    }
}
