package com.dtsx.astra.cli.utils;

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

import com.datastax.astra.client.model.CollectionIdTypes;
import com.datastax.astra.client.model.SimilarityMetric;
import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.github.rvesse.airline.parser.errors.ParseRestrictionViolatedException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Utilities for cli.
 */
public class AstraCliUtils {
    
    /** Environment variable coding user home. */
    public static final String ENV_USER_HOME = "user.home";
    
    /** Path to save third-parties. */
    public static final String ASTRA_HOME = System.getProperty(ENV_USER_HOME) + File.separator + ".astra";
    
    /** Folder name where to download SCB. */
    public static final String SCB_FOLDER = "scb";

    /**
     * Hide default constructor.
     */
    private AstraCliUtils() {}

    /**
     * Show version.
     *
     * @return
     *      return version
     */
    public static String version() {
        String versionPackage = AstraCliConsole.class
                .getPackage()
                .getImplementationVersion();
        if (versionPackage == null) {
            versionPackage = "Development";
        }
        return versionPackage;
    }

    /**
     * Parse key for environment.
     *
     * @param envKey
     *      value provided for target environment
     * @return
     *      select environment
     */
    public static AstraEnvironment parseEnvironment(String envKey) {
        try {
            if (envKey == null) envKey = "prod";
            return AstraEnvironment.valueOf(envKey.toUpperCase());
        } catch(Exception e) {
            throw new ParseRestrictionViolatedException(
                    "Invalid option value (--env), expecting "
                            + Arrays.toString(AstraEnvironment.values()));
        }
    }

    /**
     * Parse key for similarity.
     *
     * @param metric
     *      value provided for metric
     * @return
     *      select metric
     */
    public static SimilarityMetric parseMetric(String metric) {
        try {
            if (metric == null) return null;
            return SimilarityMetric.valueOf(metric.toUpperCase());
        } catch(Exception e) {
            throw new ParseRestrictionViolatedException(
                    "Invalid option value (--metric), expecting "
                            + Arrays.toString(SimilarityMetric.values()));
        }
    }

    /**
     * Parse key for defaultId.
     *
     * @param defaultId
     *      value provided for defaultId
     * @return
     *      select metric
     */
    public static CollectionIdTypes parseDefaultId(String defaultId) {
        try {
            if (defaultId == null) return null;
            return CollectionIdTypes.fromValue(defaultId);
        } catch(Exception e) {
            throw new ParseRestrictionViolatedException(
                    "Invalid option value (--default-id), expecting "
                            + Arrays.toString(CollectionIdTypes.values()));
        }
    }

    /**
     * Parse key for similarity.
     *
     * @param index
     *      value for an index
     * @return
     *      select metric
     */
    public static String[] parseIndex(String index) {
        try {
            if (index == null) return null;
            return index.split(",");
        } catch(Exception e) {
            throw new ParseRestrictionViolatedException("Invalid option value (--indexing-*), expecting an array separated by comma");
        }
    }


    /**
     * Helper to build the path (use multiple times).
     *
     * @param dId
     *      database id
     * @param dbRegion
     *      database region
     * @return
     *      filename
     */
    public static String buildScbFileName(String dId, String dbRegion) {
        return "scb_" + dId + "_" + dbRegion + ".zip";
    }
    
    /** Hold properties. */
    private static Properties properties;
    
    /**
     * Read value from application.properties.
     * 
     * @param key
     *      target key
     * @return
     *      key value
     */
    public static String readProperty(String key) {
        if (properties == null) {
            try {
                properties = new Properties();
                properties.load(AstraCli.class
                    .getClassLoader()
                    .getResourceAsStream("application.properties"));
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return properties.getProperty(key);
    }

    /**
     * Initialization of the folders at startup
     */
    public static void createHomeAstraFolders() {
        File install = new File(ASTRA_HOME);
        if (!install.exists()) install.mkdirs();
        File folderSCB = new File(ASTRA_HOME + File.separator + SCB_FOLDER);
        if (!folderSCB.exists()) folderSCB.mkdirs();
    }
}
