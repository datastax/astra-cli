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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.core.out.LoggerShell;

/**
 * Utilities methods to interact with .env file.
 */
public class EnvFile {
    
    /**
     * Valid / Supported keys in .env file
     */
     public enum EnvKey {
        /** Organization Id. */
        ASTRA_ORG_ID, 
        /** Organization Name. */
        ASTRA_ORG_NAME, 
        /** Organization Token (AS:..). */
        ASTRA_ORG_TOKEN,
        
        /** database identifier.*/
        ASTRA_DB_ID,  
        /** database region.*/
        ASTRA_DB_REGION,
        /** database keyspace.*/
        ASTRA_DB_KEYSPACE,
        /** credentials. */
        ASTRA_DB_APPLICATION_TOKEN,
        /** bundle PATH.*/
        ASTRA_DB_SECURE_BUNDLE_PATH,
        /** download link.*/
        ASTRA_DB_SECURE_BUNDLE_URL,
        /** graphql dml endpoint.*/
        ASTRA_DB_GRAPHQL_URL,
        /** graphql playground. */
        ASTRA_DB_GRAPHQL_URL_PLAYGROUND,
        /** graphql ddl endpoint. */
        ASTRA_DB_GRAPHQL_URL_SCHEMA,
        /** graphql schema-first endpoint. */
        ASTRA_DB_GRAPHQL_URL_ADMIN,
        /** rest and document Api endpoint */
        ASTRA_DB_REST_URL,
        /** swagger specification URL */
        ASTRA_DB_REST_URL_SWAGGER,
        /** Tenant Name. */
        ASTRA_STREAMING_NAME,
        /** Tenant Cloud. */
        ASTRA_STREAMING_CLOUD,
        /** Tenant deployed region. */
        ASTRA_STREAMING_REGION,
        /** Tenant pulsar_token. */
        ASTRA_STREAMING_PULSAR_TOKEN,
        /** Tenant broker_URL. */
        ASTRA_STREAMING_BROKER_URL,
        /** Tenant web service URL. */
        ASTRA_STREAMING_WEBSERVICE_URL,
        /** Tenant web socket URL. */
        ASTRA_STREAMING_WEBSOCKET_URL,


    }
    
    /** filename. */
    private static final String DOTENV_FILENAME = ".env";
    
    /** line separator. */
    public static final String ENV_LINE_SEPARATOR = "line.separator";
    
    /** line separator. */
    public static final String LINE_SEPARATOR = System.getProperty(ENV_LINE_SEPARATOR);
    
    /**
     * Keys to be populated in file. 
     */
    private final LinkedHashMap<String, String> keys = new LinkedHashMap<>();

    /**
     * Working File 
     */
    private final File dotenvFile;
    
    /**
     * Constructor.
     * 
     * @param workingFolderPath
     *      destination for file .env
     */
    public EnvFile(String workingFolderPath) {
        File workingFolder = new File(workingFolderPath);
        if (!workingFolder.exists()) {
            throw new InvalidArgumentException("Destination folder has not been found");
        }
        if (!workingFolder.isDirectory() ) {
            throw new InvalidArgumentException("Destination path is not a directory");
        }
        if (!workingFolder.canRead() || !workingFolder.canWrite() ) {
            throw new InvalidArgumentException("Cannot access destination directory, check permissions");
        }
        this.dotenvFile = new File(workingFolderPath + File.separator + DOTENV_FILENAME);
        load();
    }
    
    /**
     * Parse configuration file.
     */
    public void load() {
        if (dotenvFile.exists()) {
            LoggerShell.debug("Loading existing file");
            try(FileInputStream fis = new FileInputStream(dotenvFile)) {
                Properties p = new Properties();
                p.load(fis);
                p.forEach((key, value) -> keys.put((String) key, value.toString()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot log existing config file " + e.getMessage());
            }
        }
    }
    
    /**
     * Save .env. Key values are wrapped with quotes  when not present.
     */
    public void save() {
        try (FileWriter out = new FileWriter(dotenvFile)) {
            StringBuilder sb = new StringBuilder();
            // Sorting keys
            TreeMap<String, String> fileKey = new TreeMap<>(keys);
            fileKey.forEach((key, value) -> {
                sb.append(key);
                sb.append("=");
                if (!value.startsWith("\"")) sb.append("\"");
                sb.append(value);
                if (!value.startsWith("\"")) sb.append("\"");
                sb.append(LINE_SEPARATOR);
            });
            out.write(sb.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save dotenv file", e);
        }
    }
    
    /**
     * Add a new k.
     * 
     * @param k
     *      key
     * @param value
     *      value
     * @return
     *      reference to current object
     */
    public EnvFile add(String k, String value) {
        this.keys.put(k, value);
        return this;
    }

    /**
     * Getter accessor for attribute 'keys'.
     *
     * @return
     *       current value of 'keys'
     */
    public Map<String, String> getKeys() {
        return keys;
    }

    /**
     * Get value of dotenv file.
     *
     * @return
     *      file for dotenv
     */
    public File getDotenvFile() {
        return dotenvFile;
    }
}
    
   
            
