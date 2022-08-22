package com.datastax.astra.shell.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.datastax.astra.sdk.config.AstraClientConfig;
import com.datastax.astra.sdk.databases.domain.Database;
import com.datastax.astra.shell.AstraCli;
import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.out.LoggerShell;
import com.datastax.stargate.sdk.utils.Utils;

/**
 * Utilities to work with DSBulk.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DsBulkUtils {

    /** Version Number. */
    public static final String DSBULK_VERSION = "1.9.1";
    
    /** Archive name. */
    public static final String DSBULK_TARBALL = "dsbulk-" + DSBULK_VERSION + ".tar.gz";
    
    /** URL. */
    public static final String DSBULK_DOWNLOAD = "https://downloads.datastax.com/dsbulk/" + DSBULK_TARBALL;
    
    /** Folder name of dslbulk once untar. */
    public static final String DSBULK_FOLDER = "dsbulk-" + DSBULK_VERSION;
   
    /**
     * Hide default construtor
     */
    private DsBulkUtils() {}
    
    /**
     * Check if cqlshel has been installed.
     *
     * @return
     *      if the folder exist
     */
    public static boolean isDsBulkInstalled() {
       File dsbulkFolder = new File(AstraCli.ASTRA_HOME + File.separator + DSBULK_FOLDER);
       return dsbulkFolder.exists() && dsbulkFolder.isDirectory();
    }
    
    /**
     * Download targz and unzip.
     */
    public static void installDsBulk() {
        if (!isDsBulkInstalled()) {
            LoggerShell.info("DSBulk has not been found, downloading, please wait...");
            String destination = AstraCli.ASTRA_HOME + File.separator + DSBULK_TARBALL;
            Utils.downloadFile(DSBULK_DOWNLOAD, destination);
            File dsbulkTarball = new File (destination);
            if (dsbulkTarball.exists()) {
                LoggerShell.info("File Downloaded. Extracting archive, please wait...");
                try {
                    FileUtils.extactTargz(dsbulkTarball, new File (AstraCli.ASTRA_HOME));
                    if (isDsBulkInstalled()) {
                        // Change file permission
                        File dsBulkFile = new File(AstraCli.ASTRA_HOME + File.separator  
                                + DSBULK_FOLDER + File.separator 
                                + "bin" + File.separator  
                                + "dsbulk");
                        dsBulkFile.setExecutable(true, false);
                        dsBulkFile.setReadable(true, false);
                        dsBulkFile.setWritable(true, false);
                        
                        LoggerShell.success("DSBulk is installed");
                        dsbulkTarball.delete();
                    }
                } catch (IOException e) {
                    LoggerShell.error("Cannot extract tar archive:" + e.getMessage());
                    ExitCode.PARSE_ERROR.exit();
                }
            }
        } else {
            LoggerShell.info("DSBulk is already installed");
        }
    }

    /**
     * Install CqlShell if needed and start the program.
     * 
     * @param dsbulkParams
     *      parameters for dsbulk
     * @param db
     *      database retrieved
     * @return
     *      unix process for cqlsh
     * @throws IOException
     *      errors occured
     */
    public static Process runDsBulk(Database db, List<String> dsbulkParams) 
    throws IOException {
        List<String> commandDsbulk = new ArrayList<>();
        commandDsbulk.add(new StringBuilder()
                .append(AstraCli.ASTRA_HOME + File.separator + DSBULK_FOLDER)
                .append(File.separator + "bin")
                .append(File.separator + "dsbulk")
                .toString());
        commandDsbulk.addAll(dsbulkParams);
        commandDsbulk.add("-u");
        commandDsbulk.add("token");
        commandDsbulk.add("-p");
        commandDsbulk.add(ShellContext.getInstance().getToken());
        commandDsbulk.add("-b");
        File scb = new File(new StringBuilder()
                .append(AstraCli.ASTRA_HOME + File.separator + AstraCli.SCB_FOLDER + File.separator)
                .append(AstraClientConfig.buildScbFileName(db.getId(), db.getInfo().getRegion()))
                .toString());
        if (!scb.exists()) {
            LoggerShell.error("Cloud Secure Bundle '" + scb.getAbsolutePath() + "' has not been found.");
            ExitCode.NOT_FOUND.exit();
        }
        commandDsbulk.add(scb.getAbsolutePath());
        LoggerShell.info("RUNNING: " + StringUtils.join(commandDsbulk, " "));
        ProcessBuilder pb =  new ProcessBuilder(commandDsbulk.toArray(new String[0]));
        pb.inheritIO();
        return pb.start();
    }
    
    
}
