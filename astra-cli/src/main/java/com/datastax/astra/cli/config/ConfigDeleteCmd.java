package com.datastax.astra.cli.config;

import com.datastax.astra.cli.core.out.ShellPrinter;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a block in the command.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "delete", description = "Delete section in configuration")
public class ConfigDeleteCmd extends AbstractConfigCmd {
    
    /**
     * Section in configuration file to as as default.
     */
    @Required
    @Arguments(
       title = "sectionName", 
       description = "Section in configuration file to as as default.")
    protected String sectionName;
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws Exception {
        OperationsConfig.assertSectionExist(sectionName);
        ctx().getAstraRc().deleteSection(sectionName);
        ctx().getAstraRc().save();
        ShellPrinter.outputSuccess("Section '" + sectionName + "' has been deleted.");
    }
}
