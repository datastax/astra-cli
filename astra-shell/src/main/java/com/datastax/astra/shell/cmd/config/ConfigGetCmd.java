package com.datastax.astra.shell.cmd.config;

import java.util.Optional;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.AbstractCmd;
import com.datastax.astra.shell.out.ShellPrinter;
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
@Command(name = AbstractCmd.GET, description = "Show details for a configuration.")
public class ConfigGetCmd extends BaseConfigCommand implements Runnable {
    
    /**
     * Section in configuration file to as as default.
     */
    @Required
    @Arguments(
       title = "section", 
       description = "Section in configuration file to as as defulat.")
    protected String sectionName;
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-k", "--key" }, title = "Key in the section", description = "If provided return only value for a key.")
    protected String key;
    
    /** {@inheritDoc} */
    public void run() {
        if (!getAstraRc().isSectionExists(sectionName)) {
            ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, "Section '" + sectionName + "' has not been found in config.");
            ExitCode.INVALID_PARAMETER.exit();
        } else if (key != null) {
            Optional<String> optKey = getAstraRc().getSectionKey(sectionName, key);
            if (!optKey.isPresent()) {
                ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, 
                        "Key '" + key + "' has not been found in config section '" + sectionName + "'");
                ExitCode.INVALID_PARAMETER.exit();
            } else {
                System.out.print(optKey.get());
            }
        } else {
            System.out.print(getAstraRc().renderSection(sectionName));
        }
     }

}