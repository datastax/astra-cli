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
 * Download and start cqlShell is needed.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class CqlShellUtils {
    
    /** where to Download CqlSH. */
    public static final String CQLSH_URL = "https://downloads.datastax.com/enterprise/cqlsh-astra.tar.gz";
    
    /** Folder name of Cqlsh once untar. */
    public static final String CQLSH_FOLDER = "cqlsh-astra";
    
    /** Folder name of Cqlsh once untar. */
    public static final String CQLSH_TARBALL = "cqlsh-astra.tar.gz";
    
    /**
     * Hide default construtor
     */
    private CqlShellUtils() {}
    
    /**
     * Download targz and unzip.
     */
    public static void installCqlShellAstra() {
        if (!isCqlShellInstalled()) {
            LoggerShell.info("CqlSh has not been found, downloading, please wait...");
            String destination = AstraCli.ASTRA_HOME + File.separator + CQLSH_TARBALL;
            Utils.downloadFile(CQLSH_URL, destination);
            File cqlshtarball = new File (destination);
            if (cqlshtarball.exists()) {
                LoggerShell.info("File Downloaded. Extracting archive, please wait...");
                try {
                    FileUtils.extactTargz(cqlshtarball, new File (AstraCli.ASTRA_HOME));
                    if (isCqlShellInstalled()) {
                        // Change file permission
                        File cqlshFile = new File(AstraCli.ASTRA_HOME + File.separator  
                                + CQLSH_FOLDER + File.separator 
                                + "bin" + File.separator  
                                + "cqlsh");
                        cqlshFile.setExecutable(true, false);
                        cqlshFile.setReadable(true, false);
                        cqlshFile.setWritable(true, false);
                        
                        LoggerShell.success("Cqlsh is installed");
                        cqlshtarball.delete();
                    }
                } catch (IOException e) {
                    LoggerShell.error("Cannot extract tar archive:" + e.getMessage());
                    ExitCode.PARSE_ERROR.exit();
                }
            }
        } else {
            LoggerShell.info("Cqlsh is already installed");
        }
    }
   
    /**
     * Install CqlShell if needed and start the program.
     * 
     * @param options
     *      command to start cqlsh
     * @param db
     *      database retrieved
     * @return
     *      unix process for cqlsh
     * @throws IOException
     *      errors occured
     */
    public static Process runCqlShellAstra(CqlShellOptions options, Database db) 
    throws IOException {
        List<String> commandCqlSh = new ArrayList<>();
        commandCqlSh.add(new StringBuilder()
                .append(AstraCli.ASTRA_HOME + File.separator + CQLSH_FOLDER)
                .append(File.separator + "bin")
                .append(File.separator + "cqlsh")
                .toString());
        commandCqlSh.add("-u");
        commandCqlSh.add("token");
        commandCqlSh.add("-p");
        commandCqlSh.add(ShellContext.getInstance().getToken());
        commandCqlSh.add("-b");
        File scb = new File(new StringBuilder()
                .append(AstraCli.ASTRA_HOME + File.separator + AstraCli.SCB_FOLDER + File.separator)
                .append(AstraClientConfig.buildScbFileName(db.getId(), db.getInfo().getRegion()))
                .toString());
        if (!scb.exists()) {
            LoggerShell.error("Cloud Secure Bundle '" + scb.getAbsolutePath() + "' has not been found.");
            ExitCode.NOT_FOUND.exit();
        }
        commandCqlSh.add(scb.getAbsolutePath());
        
        // -- Custom options of Cqlsh itself
        
        if (options.isDebug()) {
            commandCqlSh.add("--debug");
        }
        if (options.isVersion()) {
            commandCqlSh.add("--version");
        }
        if (options.getExecute() != null) {
            commandCqlSh.add("-e");
            commandCqlSh.add(options.getExecute());
        }
        if (options.getFile() != null) {
            commandCqlSh.add("-f");
            commandCqlSh.add(options.getFile());
        }
        if (options.getKeyspace() != null) {
            commandCqlSh.add("-k");
            commandCqlSh.add(options.getKeyspace());
        }
        if (options.getEncoding() != null) {
            commandCqlSh.add("--encoding");
            commandCqlSh.add(options.getEncoding() );
        }
        
        LoggerShell.info("RUNNING: " + StringUtils.join(commandCqlSh, " "));
        ProcessBuilder pb =  new ProcessBuilder(commandCqlSh.toArray(new String[0]));
        pb.inheritIO();
        return pb.start();
    }
    
    /**
     * Check if cqlshel has been installed.
     *
     * @return
     *      if the folder exist
     */
    private static boolean isCqlShellInstalled() {
       File cqlshAstra = new File(AstraCli.ASTRA_HOME + File.separator + CQLSH_FOLDER);
       return cqlshAstra.exists() && cqlshAstra.isDirectory();
    }
   
}
