package com.datastax.astra.cli.cmd.config;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.out.ShellPrinter;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a block in the command.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "delete", description = "Delete section in configuration")
public class ConfigDeleteCmd extends BaseConfigCommand {
    
    /**
     * Section in configuration file to as as default.
     */
    @Required
    @Arguments(
       title = "section", 
       description = "Section in configuration file to as as default.")
    protected String sectionName;
    
    /** {@inheritDoc} */
    public void run() {
        if (!getAstraRc().isSectionExists(sectionName)) {
            ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, "Section '" + sectionName + "' has not been found in config.");
            ExitCode.INVALID_PARAMETER.exit();
        } else {
            getAstraRc().deleteSection(sectionName);
            getAstraRc().save();
            ShellPrinter.outputSuccess("Section '" + sectionName + "' has been deleted.");
        }
     }

}
