package com.datastax.astra.cli.utils;

import java.io.File;

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
}
