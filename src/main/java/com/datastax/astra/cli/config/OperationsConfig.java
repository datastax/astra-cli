package com.datastax.astra.cli.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.fusesource.jansi.Ansi;

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.exception.ConfigurationException;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.ShellTable;
import com.datastax.astra.sdk.config.AstraClientConfig;

/**
 * Group configuration actions.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class OperationsConfig {

    /** worki with roles. */
    public static final String COMMAND_CONFIG = "config";
    
    /**
     * Title of the table.
     */
    private static final String COLUMN_TITLE = "configuration";
    
    /**
     * Hide default constructor
     */
    private OperationsConfig() {}
    
    /**
     * Syntax sugar.
     *
     * @return
     *      context
     */
    private static CliContext ctx() {
        return CliContext.getInstance();
    }
    
    /**
     * Show configuration in the output.
     */
    public static void listConfigurations() {
        Map<String, Map<String, String>> sections = ctx().getConfiguration().getSections();
        List<String> orgs = listOrganizations(sections);
        ShellTable sht = new ShellTable();
        sht.setColumnTitlesColor(Ansi.Color.YELLOW);
        sht.setCellColor(Ansi.Color.WHITE);
        sht.setTableColor(Ansi.Color.CYAN);
        sht.getColumnTitlesNames().add(COLUMN_TITLE);
        sht.getColumnSize().put(COLUMN_TITLE, 40);
        for (String org : orgs) {
            Map<String, String> rf = new HashMap<>();
            rf.put(COLUMN_TITLE, org);
            sht.getCellValues().add(rf);
        }
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Build List as expected on screen.
     *
     * @param sections
     *      section in AstraRc.
     * @return
     *      organization list
     */
    public static List<String> listOrganizations(Map<String, Map<String, String>> sections) {
        List<String> returnedList = new ArrayList<>();
        Optional<String> defaultOrg = findDefaultOrganizationName(sections);
        for (Entry<String, Map<String, String>> section : sections.entrySet()) {
            if (AstraConfiguration.ASTRARC_DEFAULT.equalsIgnoreCase(section.getKey()) &&  defaultOrg.isPresent()) {
                returnedList.add(AstraConfiguration.ASTRARC_DEFAULT + " (" + defaultOrg.get() + ")");
            } else {
                returnedList.add(section.getKey());
            }
        }
        return returnedList;
    }
    
    /**
     * Find the default org name in the configuration file.
     * 
     * @param sections
     *      list of sections
     * @return
     *      organization name if exists
     */
    public static Optional<String> findDefaultOrganizationName(Map<String, Map<String, String>> sections) {
        String defaultOrgName = null;
        if (sections.containsKey(AstraConfiguration.ASTRARC_DEFAULT)) {
            String defaultToken = sections
                    .get(AstraConfiguration.ASTRARC_DEFAULT)
                    .get(AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN);
            for (Entry<String, Map<String, String>> section : sections.entrySet()) {
                if (!section.getKey().equals(AstraConfiguration.ASTRARC_DEFAULT)) {
                    if (defaultToken !=null && 
                        defaultToken.equalsIgnoreCase(
                                sections.get(section.getKey()).get(AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN))) {
                           defaultOrgName = section.getKey();
                       }
                    }
                }
        }
        return Optional.ofNullable(defaultOrgName);
    }
    
    /**
     * Test existence of section in document.
     * 
     * @param sectionName
     *      section name
     * @throws ConfigurationException
     *      configuration exception
     */
    public static void assertSectionExist(String sectionName) 
    throws ConfigurationException {
        if (!ctx().getConfiguration().isSectionExists(sectionName)) {
            throw new ConfigurationException("Section '" + sectionName + "' has not been found in config.");
        } 
    }
}
