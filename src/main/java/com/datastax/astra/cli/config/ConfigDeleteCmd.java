package com.datastax.astra.cli.config;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a block in the command.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "delete", description = "Delete section in configuration")
public class ConfigDeleteCmd extends AbstractCmd {
    
    /**
     * Section in configuration file to as default.
     */
    @Required
    @Arguments(
       title = "sectionName", 
       description = "Section in configuration file to as as default.")
    protected String sectionName;
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        OperationsConfig.assertSectionExist(sectionName);
        ctx().getConfiguration().deleteSection(sectionName);
        ctx().getConfiguration().save();
        AstraCliConsole.outputSuccess("Section '" + sectionName + "' has been deleted.");
    }
}
