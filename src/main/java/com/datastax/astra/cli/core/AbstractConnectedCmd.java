package com.datastax.astra.cli.core;

import com.datastax.astra.cli.config.AstraConfiguration;
import com.datastax.astra.cli.core.out.OutputFormat;
import com.github.rvesse.airline.annotations.Option;

import java.util.Locale;

/**
 * Base command for cli. The cli have to deal with configuration file and initialize connection
 * each item where shell already have the context initialized.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractConnectedCmd extends AbstractCmd {
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "--token" }, 
            title = "AUTH_TOKEN",
            description = "Key to use authenticate each call.")
    protected String token;

    /** Section. */
    @Option(name = { "-conf","--config" }, 
            title = "CONFIG_SECTION",
            description= "Section in configuration file (default = ~/.astrarc)")
    protected String configSectionName = AstraConfiguration.ASTRARC_DEFAULT;

    /** {@inheritDoc} */
    @Override
    public void run() {
        validateOptions();
        ctx().init(new CoreOptions(verbose, noColor, OutputFormat.valueOf(output.toUpperCase(Locale.ROOT)), configFilename));
        ctx().initToken(new TokenOptions(token, configSectionName));
        execute();
    }

}
