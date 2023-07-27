package com.dtsx.astra.cli.db.dsbulk;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exception.CannotStartProcessException;
import com.dtsx.astra.cli.core.exception.FileSystemException;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.dtsx.astra.cli.utils.ExternalSoftware;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.domain.Database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Working with external DSBulk.
 * The singleton pattern is validated with a Lazy initialization
 * and a thread safe implementation.
 */
@SuppressWarnings("java:S6548")
public class ServiceDsBulk {

    /** prefix in definition. */
    public static final String DSBULK_PREFIX = "dsbulk-";

    /** Param for dsbulk. */
    public static final String PARAM_QUERY = "-query";

    /** Param for dsbulk. */
    public static final String PARAM_DELIMITER = "-delim";

    /** Param for dsbulk. */
    public static final String PARAM_HEADER = "-header";

    /** Param for dsbulk. */
    public static final String PARAM_ENCODING = "-encoding";

    /** Param for dsbulk. */
    public static final String PARAM_URL = "-url";

    /** Param for dsbulk. */
    public static final String PARAM_SKIP_RECORDS = "-skipRecords";

    /** Param for dsbulk. */
    public static final String PARAM_MAX_ERRORS = "-maxErrors";

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
    ExternalSoftware config;
    
    /** Installation folder. */
    File dsbulkLocalFolder;

    /** Local installation for Dsbulk. */
    String dsbulkExecutable;
    
    /** Access to databases object. */
    DaoDatabase dbDao;
    
    /**
     * Singleton Pattern
     */
    private static ServiceDsBulk instance;
    
    /**
     * Singleton Pattern.
     * 
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceDsBulk getInstance() {
        if (null == instance) {
            instance = new ServiceDsBulk();
        }
        return instance;
    }

    /**
     * Initialization.
     */
    private ServiceDsBulk() {
        config = new ExternalSoftware(
                AstraCliUtils.readProperty("dsbulk.url"),
                AstraCliUtils.readProperty("dsbulk.version"));
        
        this.dbDao = DaoDatabase.getInstance();
        
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
            String zipArchive = AstraCliUtils.ASTRA_HOME + File.separator + DSBULK_PREFIX + config.version() + ".zip";
            FileUtils.downloadFile(config.url(), zipArchive);
            LoggerShell.info("Installing  archive, please wait...");
            FileUtils.extractZipArchiveInAstraCliHome(zipArchive);
            if (!new File(dsbulkExecutable).setExecutable(true, false)) {
                throw new FileSystemException("Cannot make dsbulk executable. ");
            }
            Files.delete(Paths.get(zipArchive));
        } catch (IOException e) {
            throw new FileSystemException("Cannot install DSBULK :" + e.getMessage(), e);
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
            options.add(PARAM_QUERY);
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
        options.add(new File(AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER + File.separator +
                AstraCliUtils.buildScbFileName(db.getId(), db.getInfo().getRegion())).getAbsolutePath());
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
        addLoadOptions(cmd, dsbulkCmd);
        if (null != cmd.mapping) {
            dsbulkCmd.add("-m");
            dsbulkCmd.add(cmd.mapping);
        }
        if (cmd.dryRun) dsbulkCmd.add("-dryRun");
        if (cmd.allowMissingFields) {
            dsbulkCmd.add("--schema.allowMissingFields");
            dsbulkCmd.add("true");
        }
        run(dsbulkCmd, cmd.getDb());
    }

    private static void addLoadOptions(DbLoadCmd loadCmd, List<String> dsbulkCmd) {
        dsbulkCmd.add(PARAM_DELIMITER);
        dsbulkCmd.add(loadCmd.delim);
        dsbulkCmd.add(PARAM_URL);
        dsbulkCmd.add(loadCmd.url);
        dsbulkCmd.add(PARAM_HEADER);
        dsbulkCmd.add(String.valueOf(loadCmd.header));
        dsbulkCmd.add(PARAM_ENCODING);
        dsbulkCmd.add(loadCmd.encoding);
        dsbulkCmd.add(PARAM_SKIP_RECORDS);
        dsbulkCmd.add(String.valueOf(loadCmd.skipRecords));
        dsbulkCmd.add(PARAM_MAX_ERRORS);
        dsbulkCmd.add(String.valueOf(loadCmd.maxErrors));
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
        if (null != cmd.query) {
            dsbulkCmd.add(PARAM_QUERY);
            dsbulkCmd.add(cmd.query);
        }
        run(dsbulkCmd, cmd.getDb());
    }
    
    /**
     * Run a Load command.
     * 
     * @param unloadCmd
     *      current command line
     */
    public void unload(DbUnLoadCmd unloadCmd) {
        List<String> dsbulkCmd = initCommandLine(DsBulkOperations.UNLOAD);
        addCredentialsOptions(dsbulkCmd, unloadCmd.getDb());
        addCoreOptions(dsbulkCmd, unloadCmd);
        dsbulkCmd.add(PARAM_DELIMITER);
        dsbulkCmd.add(unloadCmd.delim);
        dsbulkCmd.add(PARAM_URL);
        dsbulkCmd.add(unloadCmd.url);
        dsbulkCmd.add(PARAM_HEADER);
        dsbulkCmd.add(String.valueOf(unloadCmd.header));
        dsbulkCmd.add(PARAM_ENCODING);
        dsbulkCmd.add(unloadCmd.encoding);
        dsbulkCmd.add(PARAM_SKIP_RECORDS);
        dsbulkCmd.add(String.valueOf(unloadCmd.skipRecords));
        dsbulkCmd.add(PARAM_MAX_ERRORS);
        dsbulkCmd.add(String.valueOf(unloadCmd.maxErrors));
        if (null != unloadCmd.query) {
            dsbulkCmd.add(PARAM_QUERY);
            dsbulkCmd.add(unloadCmd.query);
        }
        run(dsbulkCmd, unloadCmd.getDb());
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

        // Install Cqlsh for Astra and set permissions
        if (!isInstalled()) install();

        // Download scb and throw DatabaseNotFound.
        dbDao.downloadCloudSecureBundles(database);

        try {
            LoggerShell.info("RUNNING: " + String.join(" ", commandDsbulk));
            LoggerShell.info("DSBulk is starting please wait ...");
            new ProcessBuilder(commandDsbulk.toArray(new String[0])).inheritIO().start().waitFor();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new CannotStartProcessException("dsbulk", e);
        }
    }

}
