package com.datastax.astra.cli.utils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.datastax.astra.cli.AstraCli;

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
    
    /** Folder name to download archives */
    public static final String TMP_FOLDER = "tmp";
    
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
