package com.datastax.astra.shell.utils;

import java.io.File;

import com.datastax.astra.shell.AstraCli;

/**
 * Utilities to work with Pulsar
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class PulsarShellUtils {
    
    /** Version Number. */
    public static final String LUNA_VERSION = "2.10.1.1";
    
    /** Archive name. */
    public static final String LUNA_TARBALL = "lunastreaming-shell-" + LUNA_VERSION + "-bin.tar.gz";
    
    /** Archive name. */
    public static final String LUNA_FOLDER = "lunastreaming-shell-" + LUNA_VERSION + "";
    
    
    /** Pulsar. */
    public static final String LUNA_URL = "https://github.com/datastax/pulsar/releases/download/ls210_1.1/" + LUNA_TARBALL;
    
    /**
     * Hide default construtor
     */
    private PulsarShellUtils() {}
    
    /**
     * Check if lunastreaming-shell has been installed.
     *
     * @return
     *      if the folder exist
     */
    public static boolean isPulsarShellInstalled() {
       File dsbulkFolder = new File(AstraCli.ASTRA_HOME + File.separator + LUNA_FOLDER);
       return dsbulkFolder.exists() && dsbulkFolder.isDirectory();
    }
    
    

}
