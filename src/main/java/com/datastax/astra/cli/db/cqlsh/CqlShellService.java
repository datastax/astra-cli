package com.datastax.astra.cli.db.cqlsh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.DatabaseDao;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.SecureBundleNotFoundException;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.datastax.astra.cli.utils.FileUtils;
import com.datastax.astra.sdk.config.AstraClientConfig;
import com.datastax.astra.sdk.databases.domain.Database;

/**
 * Working with external cqlsh.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class CqlShellService {

    /** Configutation to Donwload the archive. */
    CqlShellConfig settings;

    /** Access to Database client */
    DatabaseDao dbDao;
    
    /** Local installation for CqlSh. */
    File cqlshLocalFolder;
    
    /** Singleton Pattern. */
    private static CqlShellService instance;
    
    /**
     * Singleton Pattern.
     * 
     * @return
     *      instance of the service.
     */
    public static synchronized CqlShellService getInstance() {
        if (null == instance) {
            instance = new CqlShellService();
        }
        return instance;
    }
    
    /**
     * Default constructor
     */
    private CqlShellService() {
        this.dbDao = DatabaseDao.getInstance();
        
        settings = new CqlShellConfig(
                AstraCliUtils.readProperty("cqlsh.url"),
                AstraCliUtils.readProperty("cqlsh.tarball"));
                
        cqlshLocalFolder = new File(AstraCliUtils.ASTRA_HOME 
                + File.separator + "cqlsh-astra");
    }
    
    /**
     * Check if cqlshel has been installed.
     *
     * @return
     *      if the folder exist
     */
    public boolean isInstalled() {
       return cqlshLocalFolder.exists() && cqlshLocalFolder.isDirectory();
    }
    
    /**
     * Download targz and unzip.
     */
    public void install() {
        if (!isInstalled()) {
            LoggerShell.info("CqlSh has not been found, downloading, please wait...");
            String destination = AstraCliUtils.ASTRA_HOME + File.separator + settings.tarball();
            FileUtils.downloadFile(settings.url(), destination);
            File tarArchive = new File (destination);
            if (tarArchive.exists()) {
                LoggerShell.info("File Downloaded. Extracting archive, please wait...");
                try {
                    FileUtils.extactTargzInAstraCliHome(tarArchive);
                    if (isInstalled()) {
                        // Change file permission
                        File cqlshFile = new File(cqlshLocalFolder.getAbsolutePath() + File.separator 
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
                        if (!tarArchive.delete()) {
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
     * Initialize DSBulk command line.
     *
     * @param op
     *      current operation
     * @return
     *      command line
     */
    private List<String> buildCommandLine(CqlShellOption options, Database db) {
        List<String> cqlSh = new ArrayList<>();
        cqlSh.add(new StringBuilder()
                .append(cqlshLocalFolder.getAbsolutePath())
                .append(File.separator + "bin")
                .append(File.separator + "cqlsh")
                .toString());
        // Credentials
        cqlSh.add("-u");
        cqlSh.add("token");
        cqlSh.add("-p");
        cqlSh.add(CliContext.getInstance().getToken());
        cqlSh.add("-b");
        File scb = new File(new StringBuilder()
                .append(AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER + File.separator)
                .append(AstraClientConfig.buildScbFileName(db.getId(), db.getInfo().getRegion()))
                .toString());
        if (!scb.exists()) {
            LoggerShell.error("Cloud Secure Bundle '" + scb.getAbsolutePath() + "' has not been found.");
                    throw new SecureBundleNotFoundException(scb.getAbsolutePath());
        }
        cqlSh.add(scb.getAbsolutePath());
        
        // -- Custom options of Cqlsh itself
        
        if (options.debug()) {
            cqlSh.add("--debug");
        }
        if (options.version()) {
            cqlSh.add("--version");
        }
        if (options.execute() != null) {
            cqlSh.add("-e");
            cqlSh.add(options.execute());
        }
        if (options.file() != null) {
            cqlSh.add("-f");
            cqlSh.add(options.file());
        }
        if (options.keyspace() != null) {
            cqlSh.add("-k");
            cqlSh.add(options.keyspace());
        }
        if (options.encoding() != null) {
            cqlSh.add("--encoding");
            cqlSh.add(options.encoding() );
        }
        return cqlSh;
    }
    
    /**
     * Start CqlShell when needed.
     * 
     * @param options
     *      shell options
     * @param database
     *      current db
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     * @throws CannotStartProcessException
     *      cannot start third party process
     * @throws FileSystemException
     *      cannot access file system 
     */
    public void run(CqlShellOption options, String database) {
        
        // Install Cqlsh for Astra and set permissions
        if (!isInstalled()) {
            install();
        }
        
        // Download scb and throw DatabaseNotFound.
        dbDao.downloadCloudSecureBundles(database);    
        
        try {
            List <String > commands = buildCommandLine(options, dbDao.getDatabase(database));
            LoggerShell.info("RUNNING: " + String.join(" ", commands));
            
            System.out.println("\nCqlsh is starting please wait for connection establishment...");
            ProcessBuilder pb = new ProcessBuilder(commands.toArray(new String[0]));
            pb.inheritIO();
            Process cqlshProc = pb.start();
            if (cqlshProc == null) {
                throw new CannotStartProcessException("cqlsh");
            }
            cqlshProc.waitFor();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new CannotStartProcessException("cqlsh", e);
        }
    }

}
