package com.datastax.astra.cli.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Properties;

import com.datastax.astra.cli.core.out.LoggerShell;

/**
 * Utilities methods to interact with .env file. 
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class EnvFile {
    
    /**
     * Valid / Supported keys in .env file
     */
     public static enum EnvKey { 
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
        ASTRA_DB_CLIENT_ID,
        /** credentials. */
        ASTRA_DB_CLIENT_SECRET,
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
        /** swagger speficiation URL */
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
    };
    
    /** filename. */
    private static final String DOTENV_FILENAME = ".env";
    
    /** line separator. */
    public static final String ENV_LINE_SEPERATOR = "line.separator";
    
    /** line separator. */
    public static final String LINE_SEPARATOR = System.getProperty(ENV_LINE_SEPERATOR);
    
    /**
     * Keys to be populated in file. 
     */
    private LinkedHashMap<EnvKey, String> keys = new LinkedHashMap<>();
    
    /**
     * Working folder 
     */
    private File workingFolder;
    
    /**
     * Working File 
     */
    private File dotenvFile;
    
    /**
     * Constructor.
     * 
     * @param workingFolderPath
     *      destination for file .env
     */
    public EnvFile(String workingFolderPath) {
        this.workingFolder = new File(workingFolderPath);
        if (!workingFolder.exists()) {
            throw new IllegalArgumentException("Destination folder has not been found");
        }
        if (!workingFolder.isDirectory() ) {
            throw new IllegalArgumentException("Destination path is not a directory");
        }
        if (!workingFolder.canRead() || !workingFolder.canWrite() ) {
            throw new IllegalArgumentException("Cannot access destination directory, check permissions");
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
            try {
                Properties p = new Properties();
                p.load(new FileInputStream(dotenvFile));
                p.entrySet().stream().forEach(e -> {
                    try {
                        keys.put(EnvKey.valueOf((String) e.getKey()), e.getValue().toString());
                    } catch(IllegalArgumentException iae) {
                        // ommit invalid keys
                    }
                });
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot log existing config file " + e.getMessage());
            }
        }
    }
    
    /**
     * Save .env
     */
    public void save() {
        FileWriter out = null;
        try {
            out = new FileWriter(dotenvFile);
            StringBuilder sb = new StringBuilder();
            keys.entrySet().forEach(line -> {
                sb.append(line.getKey().name() + "=\"" + line.getValue() + "\"" + LINE_SEPARATOR);
            });
            out.write(sb.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save dotenv file", e);
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {}
            }
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
    public EnvFile add(EnvKey k, String value) {
        this.keys.put(k, value);
        return this;
    }

    /**
     * Getter accessor for attribute 'keys'.
     *
     * @return
     *       current value of 'keys'
     */
    public LinkedHashMap<EnvKey, String> getKeys() {
        return keys;
    }
}
    
   
            
