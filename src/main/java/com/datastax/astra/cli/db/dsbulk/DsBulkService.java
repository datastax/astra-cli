package com.datastax.astra.cli.db.dsbulk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.DatabaseDao;
import com.datastax.astra.cli.db.exception.SecureBundleNotFoundException;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.datastax.astra.cli.utils.FileUtils;
import com.datastax.astra.sdk.config.AstraClientConfig;
import com.datastax.astra.sdk.databases.domain.Database;

/**
 * Working with external DSBulk.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class DsBulkService  {
    
    /** Operations. */
    public enum DsBulkOperations { load, unload, count };
    
    /** DSbulk configuration. */
    DsBulkConfig config;
    
    /** Installation folder. */
    File dsbulkLocalFolder;
    
    /** Access to databases object. */
    DatabaseDao dbDao;
    
    /**
     * Singleton Pattern
     */
    private static DsBulkService instance;
    
    /**
     * Singleton Pattern.
     * 
     * @return
     *      instance of the service.
     */
    public static synchronized DsBulkService getInstance() {
        if (null == instance) {
            instance = new DsBulkService();
        }
        return instance;
    }

    /**
     * Initialization.
     */
    private DsBulkService() {
        config = new DsBulkConfig(
                AstraCliUtils.readProperty("dsbulk.url"),
                AstraCliUtils.readProperty("dsbulk.version"));
        
        this.dbDao = DatabaseDao.getInstance();
        
        this.dsbulkLocalFolder = new File(AstraCliUtils.ASTRA_HOME 
                + File.separator 
                + "dsbulk-" + config.version());
    }
    
    /**
     * Check if DSBulk is installed locally.
     * 
     * @return
     *      dskbulk folder is detected
     */
    public boolean isInstalled() {
        return dsbulkLocalFolder.exists() && 
               dsbulkLocalFolder.isDirectory();
    }
    
    /**
     * Download targz and unzip.
     * 
     * @return
     *      if an installation was required or not
     */
    public boolean install() {
        if (!isInstalled()) {
            LoggerShell.success("dsbulk first launch, downloading (~25MB), please wait...");
            String destination = AstraCliUtils.ASTRA_HOME 
                    + File.separator + "dsbulk-" 
                    + config.version() + ".tar.gz";
            FileUtils.downloadFile(config.url() + "dsbulk-" + config.version() + ".tar.gz", destination);
            File dsbulkTarball = new File (destination);
            if (dsbulkTarball.exists()) {
                LoggerShell.info("File Downloaded. Extracting archive, please wait...");
                try {
                    FileUtils.extactTargzInAstraCliHome(dsbulkTarball);
                    if (isInstalled()) {
                        // Change file permission
                        File dsBulkFile = new File(AstraCliUtils.ASTRA_HOME + File.separator  
                                + "dsbulk-" + config.version() + File.separator 
                                + "bin" + File.separator  
                                + "dsbulk");
                        if (!dsBulkFile.setExecutable(true, false)) {
                            throw new FileSystemException("Cannot set dsbulk file as executable");
                        }
                        if (!dsBulkFile.setReadable(true, false)) {
                            throw new FileSystemException("Cannot set dsbulk file as readable");
                        }
                        if (!dsBulkFile.setWritable(true, false)) {
                            throw new FileSystemException("Cannot set dsbulk file as writable");
                        }
                        LoggerShell.success("DSBulk has been installed");
                        if (!dsbulkTarball.delete()) {
                            LoggerShell.warning("DSBulk Tar archived was not deleted");
                        }
                    }
                } catch (IOException e) {
                    LoggerShell.error("Cannot extract tar archive:" + e.getMessage());
                        throw new FileSystemException("Cannot extract tar archive:" + e.getMessage(), e);
                }
            }
        } else {
            LoggerShell.info("DSBulk is already installed");
            return false;
        }
        return true;
    }

    /**
     * Initialize DSBulk command line.
     *
     * @param op
     *      current operation
     * @return
     *      command line
     */
    private List<String> initCommandLine(DsBulkOperations op) {
        List<String> dsbulk = new ArrayList<>();
        dsbulk.add(new StringBuilder()
                .append(dsbulkLocalFolder.getAbsolutePath())
                .append(File.separator + "bin")
                .append(File.separator + "dsbulk")
                .toString());
        dsbulk.add(op.name());
        return dsbulk;
    }
    
    /**
     * All DSBulkd command will start with.
     * 
     * @paran options
     *      add core options
     * @param cmd
     *      target
     * @return
     *      first part of command
     */
    private List<String> addCoreOptions(List<String> options, AbstractDsbulkCmd cmd) {
        // Keyspace
        if (null != cmd.keyspace) {
            options.add("-k");
            options.add(cmd.keyspace);
        }
        // Table
        if (null != cmd.table) {
            options.add("-t");
            options.add(cmd.table);
        }
        if (null != cmd.query) {
            options.add("-query");
            options.add(cmd.query);
        }
        // Config
        if (null != cmd.dsBulkConfig) {
            options.add("-f");
            options.add(cmd.dsBulkConfig);
        }
        // logDir
        if (null != cmd.logDir) {
            options.add("-logDir");
            options.add(cmd.logDir);
        }
        // Reducing log level
        options.add("--log.verbosity");
        options.add("normal");
        // Allo Missing fields
        options.add("--schema.allowMissingFields");
        options.add("true");
        // Concurrent queries
        options.add("-maxConcurrentQueries");
        options.add(cmd.maxConcurrentQueries);
        // User
        options.add("-u");
        options.add("token");
        // Password
        options.add("-p");
        options.add(CliContext.getInstance().getToken());
        // Cloud Secure bundle
        options.add("-b");
        Database db = dbDao.getDatabase(cmd.getDb());
        File scb = new File(new StringBuilder()
                .append(AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER + File.separator)
                .append(AstraClientConfig.buildScbFileName(db.getId(), db.getInfo().getRegion()))
                .toString());
        if (!scb.exists()) {
            throw new SecureBundleNotFoundException(scb.getAbsolutePath());
        }
        options.add(scb.getAbsolutePath());
        return options;
    }
    
    /**
     * Adding properties to load and unload.
     *
     * @param options
     *      add options for dsbulk
     * @param cmd
     *      add command for dsbulk
     */
    private void addDataOptions(List<String> options, AbstractDsbulkDataCmd cmd) {
        options.add("-delim");
        options.add(cmd.delim);
        options.add("-url");
        options.add(cmd.url);
        options.add("-header");
        options.add(String.valueOf(cmd.header));
        options.add("-encoding");
        options.add(cmd.encoding);
        options.add("-skipRecords");
        options.add(String.valueOf(cmd.skipRecords));
        options.add("-maxErrors");
        options.add(String.valueOf(cmd.maxErrors));
        if (null != cmd.mapping) {
            options.add("-m");
            options.add(cmd.mapping);
        }
    }
    
    /**
     * Run a Load command.
     * 
     * @param cmd
     *      command to be executed
     */
    public void load(DbLoadCmd cmd) {
        List<String> dsbulkCmd = initCommandLine(DsBulkOperations.load);
        addCoreOptions(dsbulkCmd, cmd);
        addDataOptions(dsbulkCmd, cmd);
        if (cmd.dryRun) {
            dsbulkCmd.add("-dryRun");   
        }
        run(dsbulkCmd, cmd.getDb());
    }
    
    /**
     * Command to count item on a table or query.
     * 
     * @param cmd
     *      current command line
     */
    public void count(DbCountCmd cmd) {
        List<String> dsbulkCmd = initCommandLine(DsBulkOperations.count);
        addCoreOptions(dsbulkCmd, cmd);
        run(dsbulkCmd, cmd.getDb());
    }
    
    /**
     * Run a Load command.
     * 
     * @param cmd
     *      current command line
     */
    public void unload(DbUnLoadCmd cmd) {
        List<String> dsbulkCmd = initCommandLine(DsBulkOperations.unload);
        addCoreOptions(dsbulkCmd, cmd);
        addDataOptions(dsbulkCmd, cmd);
        run(dsbulkCmd, cmd.getDb());
    }
    
    /**
     * Run DSBulk.
     * 
     * @param commandDsbulk
     *      current command line
     * @param dbName
     *      current db name
     */
    public void run(List<String> commandDsbulk, String dbName) {
        if (!isInstalled()) {
            install();
        }
        // Download scb (if needed)
        dbDao.downloadCloudSecureBundles(dbName);
        try {
            System.out.println("\nDSBulk is starting please wait ...");
            LoggerShell.info("RUNNING: " + String.join(" ", commandDsbulk));
            ProcessBuilder pb = new ProcessBuilder(commandDsbulk.toArray(new String[0]));
            pb.inheritIO();
            Process dsbulkProc = pb.start();
            if (dsbulkProc == null) {
                throw new CannotStartProcessException("dsbulk");
            }
            dsbulkProc.waitFor();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new CannotStartProcessException("dsbulk", e);
        }
    }

}
