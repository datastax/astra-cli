package com.datastax.astra.shell.cmd.config;

import com.datastax.astra.shell.utils.AstraRcUtils;
import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.out.ShellPrinter;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Class to set a section as default in config file
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(
    name="use", 
    description="Make a section the one used by default")
public class ConfigUseCmd extends BaseConfigCommand implements Runnable {
   
    /**
     * Section in configuration file to as as default.
     */
    @Required
    @Arguments(
       title = "section", 
       description = "Section in configuration file to as as defulat.")
    protected String sectionName;
    
    /** {@inheritDoc} */
    public void run() {
        if (!getAstraRc().isSectionExists(sectionName)) {
            ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, "Section '" + sectionName + "' has not been found in config.");
            ExitCode.INVALID_PARAMETER.exit();
        } else {
            getAstraRc().copySection(sectionName, AstraRcUtils.ASTRARC_DEFAULT);
            getAstraRc().save();
            ShellPrinter.outputSuccess("Section '" + sectionName + "' is set as default.");
        }
    }
}
