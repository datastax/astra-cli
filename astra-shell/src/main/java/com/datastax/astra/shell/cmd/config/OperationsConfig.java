package com.datastax.astra.shell.cmd.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.fusesource.jansi.Ansi;

import com.datastax.astra.sdk.config.AstraClientConfig;
import com.datastax.astra.sdk.utils.AstraRc;
import com.datastax.astra.shell.out.ShellPrinter;
import com.datastax.astra.shell.out.ShellTable;

/**
 * Group configuration actions.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class OperationsConfig {

    /**
     * Title of the table.
     */
    private static final String COLUMN_TITLE = "configuration";
    
    /**
     * Hide default constructor
     */
    private OperationsConfig() {}
    
    /**
     * Show configuration in the output.
     * 
     * @param astraRc
     *      current AstraRc
     */
    public static void listConfigurations(AstraRc astraRc) {
        Map<String, Map<String, String>> sections = astraRc.getSections();
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
        ShellPrinter.printShellTable(sht);
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
            if (AstraRc.ASTRARC_DEFAULT.equalsIgnoreCase(section.getKey()) &&  defaultOrg.isPresent()) {
                returnedList.add(AstraRc.ASTRARC_DEFAULT + " (" + defaultOrg.get() + ")");
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
        if (sections.containsKey(AstraRc.ASTRARC_DEFAULT)) {
            String defaultToken = sections
                    .get(AstraRc.ASTRARC_DEFAULT)
                    .get(AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN);
            if (defaultToken !=null) {
                for (String sectionName : sections.keySet()) {
                    if (!sectionName.equals(AstraRc.ASTRARC_DEFAULT)) {
                       if (defaultToken.equalsIgnoreCase(
                               sections.get(sectionName).get(AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN))) {
                           defaultOrgName = sectionName;
                       }
                    }
                }
            }
        }
        return Optional.ofNullable(defaultOrgName);
    }
}
