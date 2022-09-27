package com.datastax.astra.cli.config;

import com.datastax.astra.cli.core.out.ShellPrinter;
import com.datastax.astra.cli.utils.AstraRcUtils;
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
public class ConfigUseCmd extends AbstractConfigCmd implements Runnable {
   
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
        ctx().getAstraRc().copySection(sectionName, AstraRcUtils.ASTRARC_DEFAULT);
        ctx().getAstraRc().save();
        ShellPrinter.outputSuccess("Section '" + sectionName + "' is set as default.");
    }
    
    /**
     * Update property.
     * 
     * @param t
     *      current section
     * @return
     *      current reference
     */
    public ConfigUseCmd sectionName(String s) {
        this.sectionName = s;
        return this;
    }
}
