package com.datastax.astra.cli.db.dsbulk;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Working with external DSBulk.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class DsBulkService  {

    /** prefix in definition. */
    static final String DSBULK_PREFIX = "dsbulk-";

    /** Operations. */
    public enum DsBulkOperations {
        /** Load operation. */
        LOAD("load"),
        /** Unload operation. */
        UNLOAD("unload"),
        /** Count operation. */
        COUNT("count");

        /** internal op value. */
        private final String op;

        /**
         * Constructor.
         *
         * @param op
         *      value for op
         */
        DsBulkOperations(String op) {
           this.op = op;
        }

        /**
         * Getter for Op.
         *
         * @return
         *      value of op.
         */
        public String getOp() {
            return op;
        }
    }
    
    /** dsbulk configuration. */
    DsBulkConfig config;
    
    /** Installation folder. */
    File dsbulkLocalFolder;

    /** Local installation for Dsbulk. */
    String dsbulkExecutable;
    
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
                + DSBULK_PREFIX + config.version());

        this.dsbulkExecutable = dsbulkLocalFolder.getAbsolutePath() +
               File.separator + "bin" + File.separator + "dsbulk";
    }
    
    /**
     * Check if DSBulk is installed locally.
     * 
     * @return
     *      dsbulk folder is detected
     */
    public boolean isInstalled() {
        return dsbulkLocalFolder.exists() && 
               dsbulkLocalFolder.isDirectory();
    }
    
    /**
     * Download archive and extract it.
     */
    public void install() {
        try {
            LoggerShell.info("Downloading Dsbulk, please wait...");
            String tarArchive = AstraCliUtils.ASTRA_HOME + File.separator + DSBULK_PREFIX + config.version() + ".tar.gz";
            FileUtils.downloadFile(config.url() + DSBULK_PREFIX + config.version() + ".tar.gz", tarArchive);

            LoggerShell.info("Installing  archive, please wait...");
            FileUtils.extractTarArchiveInAstraCliHome(new File(tarArchive));
            if (!new File(dsbulkExecutable).setExecutable(true, false)) {
                throw new FileSystemException("Cannot make dsbulk executable. ");
            }
            if (new File(tarArchive).delete())
                LoggerShell.success("DSBulk has been installed");
        } catch (IOException e) {
            throw new FileSystemException("Cannot install CqlShell :" + e.getMessage(), e);
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
    private List<String> initCommandLine(DsBulkOperations op) {
        List<String> dsbulk = new ArrayList<>();
        dsbulk.add(dsbulkExecutable);
        dsbulk.add(op.getOp());
        return dsbulk;
    }
    
    /**
     * All dsbulk command will start with.
     * 
     * @param options
     *      add core options
     * @param cmd
     *      target
     */
    private void addCoreOptions(List<String> options, AbstractDsbulkCmd cmd) {
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
        // Allow missing fields
        options.add("--schema.allowMissingFields");
        options.add("true");
        // Concurrent queries
        options.add("-maxConcurrentQueries");
        options.add(cmd.maxConcurrentQueries);
    }

    /**
     * Add user, password and scb to command line.
     *
     * @param dbName
     *      database name
     * @param options
     *      options
     */
    private void addCredentialsOptions(List<String> options, String dbName) {
        // User
        options.add("-u");
        options.add("token");
        // Password
        options.add("-p");
        options.add(CliContext.getInstance().getToken());
        // Cloud Secure bundle
        options.add("-b");
        Database db = dbDao.getDatabase(dbName);
        File scb = new File(AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER + File.separator +
                AstraClientConfig.buildScbFileName(db.getId(), db.getInfo().getRegion()));
        if (!scb.exists()) {
            throw new SecureBundleNotFoundException(scb.getAbsolutePath());
        }
        options.add(scb.getAbsolutePath());
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
        List<String> dsbulkCmd = initCommandLine(DsBulkOperations.LOAD);
        addCredentialsOptions(dsbulkCmd, cmd.getDb());
        addCoreOptions(dsbulkCmd, cmd);
        addDataOptions(dsbulkCmd, cmd);
        if (cmd.dryRun)
            dsbulkCmd.add("-dryRun");   
        run(dsbulkCmd, cmd.getDb());
    }
    
    /**
     * Command to count item on a table or query.
     * 
     * @param cmd
     *      current command line
     */
    public void count(DbCountCmd cmd) {
        List<String> dsbulkCmd = initCommandLine(DsBulkOperations.COUNT);
        addCredentialsOptions(dsbulkCmd, cmd.getDb());
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
        List<String> dsbulkCmd = initCommandLine(DsBulkOperations.UNLOAD);
        addCredentialsOptions(dsbulkCmd, cmd.getDb());
        addCoreOptions(dsbulkCmd, cmd);
        addDataOptions(dsbulkCmd, cmd);
        run(dsbulkCmd, cmd.getDb());
    }

    /**
     * Run raw dsbulk command, astra db dsbulk dbname load -url ...
     *
     * @param options
     *      command line as provided by user dbName, dbOperation,dbOptions
     */
    public void runRaw(List<String> options) {
        List<String> commandDsbulk = new ArrayList<>();
        commandDsbulk.add(dsbulkExecutable);
        commandDsbulk.addAll(options.subList(1, options.size()));
        addCredentialsOptions(commandDsbulk, options.get(0));
        run(commandDsbulk, options.get(0));
    }

    /**
     * Run DSBulk.
     * 
     * @param commandDsbulk
     *      current command line
     * @param database
     *      current db name
     */
    public void run(List<String> commandDsbulk, String database) {
        System.out.println(commandDsbulk);

        // Install Cqlsh for Astra and set permissions
        if (!isInstalled()) install();

        // Download scb and throw DatabaseNotFound.
        dbDao.downloadCloudSecureBundles(database);

        try {
            LoggerShell.info("RUNNING: " + String.join(" ", commandDsbulk));
            LoggerShell.info("\nDSBulk is starting please wait ...");
            new ProcessBuilder(commandDsbulk.toArray(new String[0])).inheritIO().start().waitFor();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new CannotStartProcessException("dsbulk", e);
        }
    }

}
