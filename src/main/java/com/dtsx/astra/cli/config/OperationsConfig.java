package com.dtsx.astra.cli.config;

/*-
 * #%L
 * Astra Cli
 * %%
 * Copyright (C) 2022 DataStax
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exception.ConfigurationException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.sdk.utils.AstraRc;
import org.fusesource.jansi.Ansi;

import java.util.*;
import java.util.Map.Entry;

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
        List<String> listOrg = listOrganizations(sections);
        ShellTable sht = new ShellTable();
        sht.setColumnTitlesColor(Ansi.Color.YELLOW);
        sht.setCellColor(Ansi.Color.WHITE);
        sht.setTableColor(Ansi.Color.CYAN);
        sht.getColumnTitlesNames().add(COLUMN_TITLE);
        sht.getColumnSize().put(COLUMN_TITLE, 40);
        for (String org : listOrg) {
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
                    .get(AstraRc.ASTRA_DB_APPLICATION_TOKEN);
            for (Entry<String, Map<String, String>> section : sections.entrySet()) {
                if (!section.getKey().equals(AstraConfiguration.ASTRARC_DEFAULT)) {
                    if (defaultToken !=null && 
                        defaultToken.equalsIgnoreCase(
                                sections.get(section.getKey()).get(AstraRc.ASTRA_DB_APPLICATION_TOKEN))) {
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
