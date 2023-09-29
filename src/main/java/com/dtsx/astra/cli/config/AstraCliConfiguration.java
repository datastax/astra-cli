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

import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.AstraRc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Utility class to load/save .astrarc file. This file is used to store Astra configuration.
 */
public class AstraCliConfiguration {
    
    /** Default filename we are looking for. */
    public static final String ASTRARC_FILENAME = ".astrarc";

    /** Default filename we are looking for. */
    public static final String ASTRARC_DEFAULT = "default";

    /** Environment variable coding user home. */
    public static final String ENV_USER_HOME = "user.home";

    /** line separator. */
    public static final String ENV_LINE_SEPARATOR = "line.separator";
    
    /** line separator. */
    public static final String LINE_SEPARATOR = System.getProperty(ENV_LINE_SEPARATOR);

    /**
     * If provided in the section, it will target another environment than PROD.
     */
    public static final String KEY_ENV = "ASTRA_ENV";

    /** Sections in the file. [sectionName] -> key=Value. */
    private final Map<String, Map<String, String>> sections = new HashMap<>();
    
    /** Working configuration file to save keys. */
    private final File file;
     
    /**
     * Load from ~/.astrarc
     */
    public AstraCliConfiguration() {
        this(getDefaultConfigurationFileName());
    }
    
    /**
     * Load from specified file
     * 
     * @param fileName
     *          configuration file name
     */
    public AstraCliConfiguration(String fileName) {
        this.file = new File(fileName);
        if (!file.exists()) {
            createConfigFileIfNotExists();
        }
        LoggerShell.debug("Configuration: Parsing file %s ...".formatted(fileName));
        parseConfigFile();
        LoggerShell.debug("Configuration: [OK] Configurations are %s".formatted(sections.keySet().toString()));
    }

    /**
     * Build default configuration filename.
     * 
     * @return
     *      default configuration file name
     */
    public static String getDefaultConfigurationFileName() {
        return System.getProperty(ENV_USER_HOME) + File.separator + ASTRARC_FILENAME;
    }
    
    /**
     * Test session existence.
     * 
     * @param sectionName
     *      section name
     * @return
     *      tell if the section exists
     */
    public boolean isSectionExists(String sectionName) {
        return sectionName != null && sections.containsKey(sectionName);
    }

    /**
     * Getter accessor for attribute 'sections'.
     *
     * @return current value of 'sections'
     */
    public Map<String, Map<String, String>> getSections() {
        return sections;
    }
    
    /**
     * Access a session from its name.
     *
     * @param sectionName
     *      section name
     * @return
     *      keys for this section
     */
    public Map<String, String> getSection(String sectionName) {
        if (isSectionExists(sectionName)) {
            return sections.get(sectionName);
        }
        return new HashMap<>();
    }
    
    /**
     * Delete a section if exists.
     * 
     * @param sectionName
     *      current name.
     */
    public void deleteSection(String sectionName) {
        boolean shouldDelete = isSectionExists(sectionName);
        if (shouldDelete) 
            sections.remove(sectionName);
    }

    /**
     * Read a key for a section
     * 
     * @param sectionName
     *            String
     * @param key
     *            String
     * @return String
     */
    public Optional<String> getSectionKey(String sectionName, String key) {
        Optional<String> result = Optional.empty();
        if (isSectionExists(sectionName))
            result = Optional.ofNullable(sections.get(sectionName).get(key));
        return result;
    }
    
    /**
     * Update only one key.
     * 
     * @param sectionName
     *            String
     * @param key
     *            String
     * @param value
     *            String
     */
    public void updateSectionKey(String sectionName, String key, String value) {
        if (!isSectionExists(sectionName))
            sections.put(sectionName, new HashMap<>());
        sections.get(sectionName).put(key, value);
    }

    /**
     * Copy a section with all those key in another.
     * 
     * @param sectionOld
     *      old section name
     * @param sectionNew
     *      new section name
     */
    public void copySection(String sectionOld, String sectionNew) {
        if (isSectionExists(sectionOld)) {
            sections.remove(sectionNew);
            sections.put(sectionNew, new HashMap<>());
            getSection(sectionOld).forEach(
                    (key, value) -> sections.get(sectionNew).put(key, value));
        }
    }
    
    /**
     * Create configuration file if not exist.
     */
    private void createConfigFileIfNotExists() {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    LoggerShell.debug("File Created");
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create configuration file: " + file.getPath());
            }
        }
    }

    /**
     * Create configuration file with current sections.
     */
    public void save() {
        try (FileWriter out = new FileWriter(file)) {
            out.write(renderSections());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save configuration file", e);
        }
    }

    /**
     * Load configuration file.
     */
    private void parseConfigFile() {
        try (Scanner scanner = new Scanner(file)) {
            if (file.exists()) {
                String sectionName = "";
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("[")) {
                        // Starting a new section
                        sectionName = line.replace("[", "").replace("]", "").trim();
                        sections.put(sectionName, new HashMap<>());
                    } else if (!line.isEmpty() && !line.startsWith("#")) {
                        int off = line.indexOf("=");
                        if (off < 0) {
                            throw new IllegalArgumentException(
                                    "Cannot parse file " + file.getName() + ", line '" + line + "' invalid format expecting key=value");
                        }
                        String key = line.substring(0, off);
                        String val = line.substring(off + 1);
                        sections.get(sectionName).put(key, val);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Cannot read configuration file", e);
        }
    }
    
    /**
     * Prepare file content
     * 
     * @return
     *      sections as a string
     */
    public String renderSections() {
        StringBuilder sb = new StringBuilder();
        sections.keySet().forEach(s -> sb.append(renderSection(s)));
        return sb.toString();
    }
    
    /**
     * Display section as a string.
     *
     * @param sectionName
     *      name of section
     * @return
     *      section as a string
     */
    public String renderSection(String sectionName) {
        StringBuilder sb = new StringBuilder();
        if (sectionName!= null && sections.containsKey(sectionName)) {
            sb.append(LINE_SEPARATOR).append("[").append(sectionName).append("]").append(LINE_SEPARATOR);
            sections.get(sectionName).forEach(
                    (key, value) -> sb.append(key).append("=").append(value).append(LINE_SEPARATOR));
        }
        return sb.toString();
    }

    /**
     * Create a section in the configuration file.
     *
     * @param sectionName
     *      current section name
     * @param token
     *      token to authenticate
     */
    public void createSectionWithToken(String sectionName, String token, AstraEnvironment env) {
        updateSectionKey(sectionName, AstraRc.ASTRA_DB_APPLICATION_TOKEN, token);
        if (AstraEnvironment.PROD != env) {
            updateSectionKey(sectionName, KEY_ENV, env.name());
        }
        if (!isSectionExists(ASTRARC_DEFAULT))
            copySection(sectionName, ASTRARC_DEFAULT);
    }

}
