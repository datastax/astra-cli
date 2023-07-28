package com.dtsx.astra.cli.config;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import com.dtsx.astra.cli.core.out.AstraAnsiColors;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.core.out.StringBuilderAnsi;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraRc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Group configuration actions.
 */
public class ServiceConfig {

    /**
     * Title of the table.
     */
    private static final String COLUMN_TITLE = "configuration";

    /**
     * Title of environment column.
     */
    private static final String COLUMN_ENV = "env";
    
    /**
     * Hide default constructor
     */
    private ServiceConfig() {}
    
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
        ShellTable sht = new ShellTable();
        boolean isMultiEnv = isMultiEnvironment(sections);
        if (isMultiEnv) {
            sht.getColumnTitlesNames().add(COLUMN_ENV);
            sht.getColumnSize().put(COLUMN_ENV, 5);
        }
        sht.getColumnTitlesNames().add(COLUMN_TITLE);
        sht.getColumnSize().put(COLUMN_TITLE, 40);
        // Find Token in use (default)
        Optional<String> defaultToken = Optional.empty();
        if (sections.containsKey(AstraCliConfiguration.ASTRARC_DEFAULT)) {
            defaultToken = Optional.of(sections
                    .get(AstraCliConfiguration.ASTRARC_DEFAULT)
                    .get(AstraRc.ASTRA_DB_APPLICATION_TOKEN));
        }
        for (Map.Entry<String, Map<String, String>> section : sections.entrySet()) {
            if (!AstraCliConfiguration.ASTRARC_DEFAULT.equalsIgnoreCase(section.getKey())) {
                Map<String, String> rf = new HashMap<>();
                String currentToken = sections.get(section.getKey()).get(AstraRc.ASTRA_DB_APPLICATION_TOKEN);
                if (defaultToken.isPresent() && defaultToken.get().equals(currentToken)) {
                    rf.put(COLUMN_TITLE, StringBuilderAnsi.colored(section.getKey() + " (in use)", AstraAnsiColors.PURPLE_300));
                } else {
                    rf.put(COLUMN_TITLE, section.getKey());
                }
                if (isMultiEnv) {
                    rf.put(COLUMN_ENV, Optional.ofNullable(sections.get(section.getKey())
                                    .get(AstraCliConfiguration.KEY_ENV))
                            .orElse(ApiLocator.AstraEnvironment.PROD.name()));
                }
                sht.getCellValues().add(rf);
            }
        }
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Show configuration in the output.
     *
     * @param sections
     *     sections in AstraRc.
     */
    public static boolean isMultiEnvironment(Map<String, Map<String, String>> sections) {
        return sections.values().stream()
                .flatMap(map -> map.keySet().stream())
                .anyMatch(key -> key.equals(AstraCliConfiguration.KEY_ENV));
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
