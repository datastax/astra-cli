package com.datastax.astra.cli.cmd.config;


import com.datastax.astra.cli.cmd.AbstractCmd;
import com.datastax.astra.cli.utils.AstraRcUtils;
import com.github.rvesse.airline.annotations.Option;

/**
 * Shared properties with multiple config command (avoiding duplication).
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class BaseConfigCommand extends AbstractCmd {
    
    /** worki with roles. */
    public static final String COMMAND_CONFIG = "config";
    
    /**
     * File on disk to reuse configuration.
     */
    @Option(name = { "--config-file" }, 
            title = "CONFIG_FILE",
            description= "Configuration file (default = ~/.astrarc)")
    protected String configFilename = AstraRcUtils.getDefaultConfigurationFileName();
    
    /**
     * Configuration loaded
     */
    protected AstraRcUtils astraRc;
    
    /**
     * Getter for confifguration AstraRC.
     *
     * @return
     *      configuration in AstraRc
     */
    protected AstraRcUtils getAstraRc() {
        if (astraRc == null) {
            if (configFilename != null) {
                astraRc = new AstraRcUtils(configFilename);
            } else {
                astraRc = new AstraRcUtils();
            }
        }
        return astraRc;
    }

}
