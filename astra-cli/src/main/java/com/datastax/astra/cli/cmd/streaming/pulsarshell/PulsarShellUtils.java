package com.datastax.astra.cli.cmd.streaming.pulsarshell;

import java.io.File;
import java.io.IOException;

import com.datastax.astra.cli.AstraCli;
import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.out.LoggerShell;
import com.datastax.astra.cli.utils.FileUtils;

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
       File pulsarShellFolder = new File(AstraCli.ASTRA_HOME + File.separator + LUNA_FOLDER);
       return pulsarShellFolder.exists() && pulsarShellFolder.isDirectory();
    }
    
    /**
     * Download targz and unzip.
     */
    public static void installPulsarShell() {
        if (!isPulsarShellInstalled()) {
            LoggerShell.info("pulsar-shell has not been found, downloading, please wait...");
            String destination = AstraCli.ASTRA_HOME + File.separator + LUNA_TARBALL;
            FileUtils.downloadFile(LUNA_URL, destination);
            File pulsarShelltarball = new File (destination);
            if (pulsarShelltarball.exists()) {
                LoggerShell.info("File Downloaded. Extracting archive, please wait...");
                try {
                    FileUtils.extactTargz(pulsarShelltarball, new File (AstraCli.ASTRA_HOME));
                    if (isPulsarShellInstalled()) {
                        // Change file permission
                        File pulsarShellFile = new File(AstraCli.ASTRA_HOME + File.separator  
                                + LUNA_FOLDER + File.separator 
                                + "bin" + File.separator  
                                + "pulsar-shell");
                        pulsarShellFile.setExecutable(true, false);
                        pulsarShellFile.setReadable(true, false);
                        pulsarShellFile.setWritable(true, false);
                        
                        LoggerShell.success("pulsar-shell has been installed");
                        pulsarShellFile.delete();
                    }
                } catch (IOException e) {
                    LoggerShell.error("Cannot extract tar archive:" + e.getMessage());
                    ExitCode.PARSE_ERROR.exit();
                }
            }
        } else {
            LoggerShell.info("pulsar-shell is already installed");
        }
    }

}
