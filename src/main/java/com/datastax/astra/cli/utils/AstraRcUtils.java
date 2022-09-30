/*

 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.datastax.astra.cli.utils;

import static com.datastax.astra.sdk.config.AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN;

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
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class AstraRcUtils {
    
    /** Default filename we are looking for. */
    public static final String ASTRARC_FILENAME = ".astrarc";

    /** Default filename we are looking for. */
    public static final String ASTRARC_DEFAULT = "default";

    /** Environment variable coding user home. */
    public static final String ENV_USER_HOME = "user.home";

    /** line separator. */
    public static final String ENV_LINE_SEPERATOR = "line.separator";
    
    /** line separator. */
    public static final String LINE_SEPARATOR = System.getProperty(ENV_LINE_SEPERATOR);

    /** Sections in the file. [sectionName] -> key=Value. */
    private final Map<String, Map<String, String>> sections = new HashMap<>();
    
    /** Working configuration file to save keys. */
    private File configFile;
     
    /**
     * Load from ~/.astrarc
     */
    public AstraRcUtils() {
        this(getDefaultConfigurationFileName());
    }
    
    /**
     * Load from specified file
     * 
     * @param fileName
     *            String
     */
    public AstraRcUtils(String fileName) {
        this.configFile = new File(fileName);
        if (!configFile.exists()) {
            createConfigFileIfNotExists();
        }
        parseConfigFile();
    }
    
    /**
     * Assess if default config exist.
     * 
     * @return
     *      if default config exists
     */
    public static boolean isDefaultConfigFileExists() {
        return new File(getDefaultConfigurationFileName()).exists();
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
        return null;
    }
    
    /**
     * Delete a section is exist.
     * 
     * @param sectionName
     *      current name.
     * @return
     *      if delete or not
     */
    public boolean deleteSection(String sectionName) {
        boolean should_delete = isSectionExists(sectionName);
        if (should_delete) {
            sections.remove(sectionName);
        }
        return should_delete;
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
        if (isSectionExists(sectionName)) {
            result = Optional.ofNullable(sections.get(sectionName).get(key));
        }
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
        if (!isSectionExists(sectionName)) {
            sections.put(sectionName, new HashMap<>());
        }
        sections.get(sectionName).put(key, value);
    }
    
    /**
     * Renaming a section (if exist)
     * 
     * @param sectionOld
     *      old name
     * @param sectionNew
     *      new section name
     */
    public void renameSection(String sectionOld, String sectionNew) {
        copySection(sectionOld, sectionNew);
        sections.remove(sectionOld);
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
            getSection(sectionOld).entrySet().forEach(m -> {
                sections.get(sectionNew).put(m.getKey(), m.getValue());
            });
        }
    }   
    
    /**
     * Create configuration file if not exist.
     * 
     * @return
     *      if the file has been created
     */
    private boolean createConfigFileIfNotExists() {
        if (!configFile.exists()) {
            try {
                return configFile.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create configuration file " + configFile.getPath());
            }
        }
        return false;
    }

    /**
     * Create configuration file with current sections.
     */
    public void save() {
        FileWriter out = null;
        try {
            out = new FileWriter(configFile);
            out.write(renderSections());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save configuration file", e);
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {}
            }
        }
    }

    /**
     * Load configuration file.
     *  
     * @param file
     *      configuration file
     */
    private void parseConfigFile() {
        try (Scanner scanner = new Scanner(configFile)) {
            if (configFile.exists()) {
                String sectionName = "";
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("[")) {
                        // Starting a new section
                        sectionName = line.replaceAll("\\[", "").replaceAll("\\]", "").trim();
                        sections.put(sectionName, new HashMap<>());
                    } else if (!line.isEmpty() && !line.startsWith("#") && !"".equals(line)) {
                        int off = line.indexOf("=");
                        if (off < 0) {
                            throw new IllegalArgumentException(
                                    "Cannot parse file " + configFile.getName() + ", line '" + line + "' invalid format expecting key=value");
                        }
                        String key = line.substring(0, off);
                        String val = line.substring(off + 1);
                        sections.get(sectionName).put(key, val);
                    }
                }
                scanner.close();
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
            sb.append(LINE_SEPARATOR + "[" + sectionName + "]" + LINE_SEPARATOR);
            sections.get(sectionName).entrySet().forEach(line -> {
                sb.append(line.getKey() + "=" + line.getValue() + LINE_SEPARATOR);
            });
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
    public void createSectionWithToken(String sectionName, String token) {
        updateSectionKey(sectionName, ASTRA_DB_APPLICATION_TOKEN, token);
        if (!isSectionExists(ASTRARC_DEFAULT)) {
            copySection(sectionName, ASTRARC_DEFAULT);
        }
    }

    /**
     * Getter accessor for attribute 'configFile'.
     *
     * @return
     *       current value of 'configFile'
     */
    public File getConfigFile() {
        return configFile;
    }   

}
