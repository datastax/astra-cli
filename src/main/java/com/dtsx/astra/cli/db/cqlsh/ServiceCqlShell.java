package com.dtsx.astra.cli.db.cqlsh;

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
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.domain.Database;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Working with external cqlsh.
 * The singleton pattern is validated with a Lazy initialization
 * and a thread safe implementation.
 */
@SuppressWarnings("java:S6548")
public class ServiceCqlShell {

    /** Configuration to Download the archive. */
    CqlShellConfig settings;

    /** Access to Database client */
    DaoDatabase dbDao;
    
    /** Local installation folder for CqlSh. */
    File cqlshLocalFolder;

    /** Local installation for CqlSh. */
    String cqlshExecutable;

    /** Singleton Pattern. */
    private static ServiceCqlShell instance;

    /**
     * Singleton Pattern.
     * 
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceCqlShell getInstance() {
        if (null == instance) {
            instance = new ServiceCqlShell();
        }
        return instance;
    }
    
    /**
     * Default constructor
     */
    private ServiceCqlShell() {
        this.dbDao = DaoDatabase.getInstance();
        
        settings = new CqlShellConfig(
                AstraCliUtils.readProperty("cqlsh.url"),
                AstraCliUtils.readProperty("cqlsh.tarball"));

        cqlshLocalFolder = new File(AstraCliUtils.ASTRA_HOME + File.separator + "cqlsh-astra");

        cqlshExecutable = cqlshLocalFolder.getAbsolutePath() + File.separator + "bin" + File.separator + "cqlsh";
    }

    /**
     * Check if cqlshell has been installed.
     *
     * @return
     *      if the folder exist
     */
    public boolean isInstalled() {
       return cqlshLocalFolder.exists() && cqlshLocalFolder.isDirectory();
    }

    /**
     * Download tar archive and decompress.
     */
    public void install() {
        try {
            LoggerShell.info("Downloading Cqlshell, please wait...");
            String tarArchive = AstraCliUtils.ASTRA_HOME + File.separator + settings.tarball();
            FileUtils.downloadFile(settings.url(), tarArchive);

            LoggerShell.info("Installing  archive, please wait...");
            FileUtils.extractTarArchiveInAstraCliHome(new File(tarArchive));
            if (!new File(cqlshExecutable).setExecutable(true, false)) {
                throw new FileSystemException("Cannot make cqlshell executable. ");
            }
            Files.delete(Paths.get(tarArchive));
        } catch (IOException e) {
            throw new FileSystemException("Cannot install CqlShell :" + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize CqlShell command line.
     *
     * @param options
     *      options in the command line
     * @param db
     *      current database
     * @return
     *      command line
     */
    private List<String> buildCommandLine(CqlShellOption options, Database db) {
        List<String> cqlSh = new ArrayList<>();
        cqlSh.add(cqlshExecutable);
        // Credentials
        cqlSh.add("-u");cqlSh.add("token");
        cqlSh.add("-p");cqlSh.add(CliContext.getInstance().getToken());
        cqlSh.add("-b");
        cqlSh.add(new File(AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER + File.separator +
                AstraCliUtils.buildScbFileName(db.getId(), db.getInfo().getRegion())).getAbsolutePath());
        
        // -- Custom options of Cqlsh itself
        if (options.debug())
            cqlSh.add("--debug");
        if (options.version())
            cqlSh.add("--version");
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
        // Timeout
        cqlSh.add("--connect-timeout");cqlSh.add(String.valueOf(options.connectTimeout()));
        cqlSh.add("--request-timeout");cqlSh.add(String.valueOf(options.requestTimeout()));
        if (options.execute() != null) {
            cqlSh.add("-e");
            cqlSh.add(options.execute());
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
     */
    public void run(CqlShellOption options, String database) {
        
        // Install Cqlsh for Astra and set permissions
        if (!isInstalled()) install();
        
        // Download scb and throw DatabaseNotFound.
        dbDao.downloadCloudSecureBundles(database);

        try {
            List <String > commands = buildCommandLine(options, dbDao.getDatabase(database));
            LoggerShell.debug("RUNNING: " + String.join(" ", commands));
            LoggerShell.info("Cqlsh is starting, please wait for connection establishment...");
            new ProcessBuilder(commands.toArray(new String[0])).inheritIO().start().waitFor();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new CannotStartProcessException("cqlsh", e);
        }
    }

}
