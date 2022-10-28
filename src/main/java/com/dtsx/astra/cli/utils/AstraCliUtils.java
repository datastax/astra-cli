package com.dtsx.astra.cli.utils;

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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.core.out.AstraCliConsole;

/**
 * Utilities for cli. 
 * 
 * @author Cedrick LUNVEN (@clunven)
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
    public static Properties properties;
    
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
    
}
