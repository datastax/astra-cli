package com.datastax.astra.cli.config;

import java.util.Optional;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.core.exception.ConfigurationException;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Allowing both syntax:
 * 
 * astra config show default
 * astra show config default 
 */
@Command(name = "get", description = "Show details for a configuration.")
public class ConfigGetCmd extends AbstractCmd {
    
    /**
     * Section in configuration file to as as default.
     */
    @Required
    @Arguments(
       title = "sectionName", 
       description = "Section in configuration file to as as default.")
    protected String sectionName;
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-k", "--key" }, title = "Key in the section", description = "If provided return only value for a key.")
    protected String key;
    
    /** {@inheritDoc}  */
    @Override
    public void execute() throws ConfigurationException  {
        OperationsConfig.assertSectionExist(sectionName);
        if (key != null) {
            Optional<String> optKey = ctx().getConfiguration().getSectionKey(sectionName, key);
            if (optKey.isEmpty()) {
                AstraCliConsole.outputError(
                        ExitCode.INVALID_PARAMETER, 
                        "Key '" + key + "' has not been found in config section '" + sectionName + "'");
                throw new ConfigurationException("Key '" + key + "' has not been found in config section '" + sectionName + "'");
            } else {
                AstraCliConsole.println(optKey.get());
            }
        } else {
            AstraCliConsole.println(ctx().getConfiguration().renderSection(sectionName));
        }
     }
    
    /**
     * Update property.
     * 
     * @param s
     *      current section
     * @return
     *      current reference
     */
    public ConfigGetCmd sectionName(String s) {
        this.sectionName = s;
        return this;
    }

}