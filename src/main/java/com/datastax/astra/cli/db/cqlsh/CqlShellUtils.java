package com.datastax.astra.cli.db.cqlsh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.exception.SecureBundleNotFoundException;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.datastax.astra.cli.utils.FileUtils;
import com.datastax.astra.sdk.config.AstraClientConfig;
import com.datastax.astra.sdk.databases.domain.Database;

/**
 * Download and start cqlShell is needed.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class CqlShellUtils {
    
    /** where to Download CqlSH. */
    public static final String CQLSH_URL = AstraCliUtils.readProperty("cqlsh.url");
    
    /** Folder name of Cqlsh once untar. */
    public static final String CQLSH_FOLDER = AstraCliUtils.readProperty("cqlsh.folder");
    
    /** Folder name of Cqlsh once untar. */
    public static final String CQLSH_TARBALL = AstraCliUtils.readProperty("cqlsh.tarball");
    
    /**
     * Hide default construtor
     */
    private CqlShellUtils() {}
    
    /**
     * Download targz and unzip.
     *
     * @throws FileSystemException
     *      error when accessing file system 
     */
    public static void installCqlShellAstra() 
    throws FileSystemException {
        if (!isCqlShellInstalled()) {
            LoggerShell.info("CqlSh has not been found, downloading, please wait...");
            String destination = AstraCliUtils.ASTRA_HOME + File.separator + CQLSH_TARBALL;
            FileUtils.downloadFile(CQLSH_URL, destination);
            File cqlshtarball = new File (destination);
            if (cqlshtarball.exists()) {
                LoggerShell.info("File Downloaded. Extracting archive, please wait...");
                try {
                    FileUtils.extactTargzInAstraCliHome(cqlshtarball);
                    if (isCqlShellInstalled()) {
                        // Change file permission
                        File cqlshFile = new File(AstraCliUtils.ASTRA_HOME + File.separator  
                                + CQLSH_FOLDER + File.separator 
                                + "bin" + File.separator  
                                + "cqlsh");
                        if (!cqlshFile.setExecutable(true, false)) {
                            throw new FileSystemException("Cannot set cqlsh file as executable");
                        }
                        if (!cqlshFile.setReadable(true, false)) {
                            throw new FileSystemException("Cannot set cqlsh file as readable");
                        }
                        if (!cqlshFile.setWritable(true, false)) {
                            throw new FileSystemException("Cannot set cqlsh file as writable");
                        }
                        LoggerShell.success("Cqlsh has been installed");
                        if (!cqlshtarball.delete()) {
                            LoggerShell.warning("Cqlsh Tar archived was not deleted");
                        }
                    }
                } catch (IOException e) {
                    LoggerShell.error("Cannot extract tar archive:" + e.getMessage());
                    throw new FileSystemException("Cannot extract tar archive:" + e.getMessage(), e);
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
     * @throws SecureBundleNotFoundException
     *      secure connec bundle has not been found
     */
    public static Process runCqlShellAstra(CqlShellOption options, Database db) 
    throws IOException, SecureBundleNotFoundException {
        List<String> commandCqlSh = new ArrayList<>();
        commandCqlSh.add(new StringBuilder()
                .append(AstraCliUtils.ASTRA_HOME + File.separator + CQLSH_FOLDER)
                .append(File.separator + "bin")
                .append(File.separator + "cqlsh")
                .toString());
        commandCqlSh.add("-u");
        commandCqlSh.add("token");
        commandCqlSh.add("-p");
        commandCqlSh.add(ShellContext.getInstance().getToken());
        commandCqlSh.add("-b");
        File scb = new File(new StringBuilder()
                .append(AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER + File.separator)
                .append(AstraClientConfig.buildScbFileName(db.getId(), db.getInfo().getRegion()))
                .toString());
        if (!scb.exists()) {
            LoggerShell.error("Cloud Secure Bundle '" + scb.getAbsolutePath() + "' has not been found.");
                    throw new SecureBundleNotFoundException(scb.getAbsolutePath());
        }
        commandCqlSh.add(scb.getAbsolutePath());
        
        // -- Custom options of Cqlsh itself
        
        if (options.debug()) {
            commandCqlSh.add("--debug");
        }
        if (options.version()) {
            commandCqlSh.add("--version");
        }
        if (options.execute() != null) {
            commandCqlSh.add("-e");
            commandCqlSh.add(options.execute());
        }
        if (options.file() != null) {
            commandCqlSh.add("-f");
            commandCqlSh.add(options.file());
        }
        if (options.keyspace() != null) {
            commandCqlSh.add("-k");
            commandCqlSh.add(options.keyspace());
        }
        if (options.encoding() != null) {
            commandCqlSh.add("--encoding");
            commandCqlSh.add(options.encoding() );
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
    public static boolean isCqlShellInstalled() {
       File cqlshAstra = new File(AstraCliUtils.ASTRA_HOME + File.separator + CQLSH_FOLDER);
       return cqlshAstra.exists() && cqlshAstra.isDirectory();
    }
   
}
