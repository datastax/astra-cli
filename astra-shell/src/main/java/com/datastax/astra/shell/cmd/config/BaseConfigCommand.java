package com.datastax.astra.shell.cmd.config;

import com.datastax.astra.sdk.utils.AstraRc;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.github.rvesse.airline.annotations.Option;

/**
 * Shared properties with multiple config command (avoiding duplication).
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class BaseConfigCommand extends BaseCommand {
    
    /** worki with roles. */
    public static final String COMMAND_CONFIG = "config";
    
    /**
     * File on disk to reuse configuration.
     */
    @Option(name = { "--config-file" }, 
            title = "CONFIG_FILE",
            description= "Configuration file (default = ~/.astrarc)")
    protected String configFilename = AstraRc.getDefaultConfigurationFileName();
    
    /**
     * Configuration loaded
     */
    protected AstraRc astraRc;
    
    /**
     * Getter for confifguration AstraRC.
     *
     * @return
     *      configuration in AstraRc
     */
    protected AstraRc getAstraRc() {
        if (astraRc == null) {
            if (configFilename != null) {
                astraRc = new AstraRc(configFilename);
            } else {
                astraRc = new AstraRc();
            }
        }
        return astraRc;
    }

}
