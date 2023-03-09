package com.dtsx.astra.cli.db.migration;

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
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.db.dsbulk.ServiceDsBulk;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.dtsx.astra.cli.utils.ExternalSoftware;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.domain.Database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Interaction wit external DSBulk Migrator
 */
public class ServiceDsBulkMigrator implements DsBulkMigratorParameters {

    /** prefix in definition. */
    static String DSBULK_MIGRATOR_PREFIX = "dsbulk-migrator-";

    /**
     * Singleton Pattern
     */
    private static ServiceDsBulkMigrator instance;

    /**
     * Singleton Pattern.
     *
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceDsBulkMigrator getInstance() {
        if (null == instance) {
            instance = new ServiceDsBulkMigrator();
        }
        return instance;
    }

    /** dsbulk-migrator configuration. */
    final ExternalSoftware config;

    /** dsbulk service. */
    final ServiceDsBulk dsbulkService;

    /**
     * Initialization.
     */
    private ServiceDsBulkMigrator() {
        this.config = new ExternalSoftware(
                AstraCliUtils.readProperty("dsbulk.migrator.url"),
                AstraCliUtils.readProperty("dsbulk.migrator.version"));
        this.dsbulkService = ServiceDsBulk.getInstance();
    }

    /**
     * Check if DSBulk is installed locally.
     *
     * @return
     *      dsbulk folder is detected
     */
    public boolean isInstalled() {
        File installDir = getDsBulkMigratorInstallationFolder();
        return installDir.exists() && installDir.isDirectory();
    }

    /**
     * Download archive and extract it.
     */
    public void install() {
        LoggerShell.info("Installation Dsbulk-Migrator, please wait...");
        // Force folder creations
        if (getDsBulkMigratorInstallationFolder().mkdirs()) {
            LoggerShell.info("Folder created");
        }
        // Download file there
        FileUtils.downloadFile(getDsBulkMigratorJarUrl(), getDsBulkMigratorJarLocalPath());
        LoggerShell.info("File downloaded");
    }

    private List<String> startOptions() {
        List<String> options = new ArrayList<>();
        options.add("java");
        options.add("-jar");
        options.add(getDsBulkMigratorJarLocalPath());
        return options;
    }

    /**
     * Generate Script for a migration.
     *
     * @param genScriptOptions
     *      generate script (leveraging Astra CLI ?)
     */
    public void generateScript(GenerateScriptOptions genScriptOptions) {
        List<String> options = startOptions();
        // All same options of generate DDL

        // Not using dsbulk embedded but the one already there
        if (!dsbulkService.isInstalled())  dsbulkService.install();
        options.add(OPTION_DSBULK_CMD);
        options.add(dsbulkService.getDsbulkExecutable());


        run(options, genScriptOptions.getDb());

    }

    /**
     * Generate DDL for a database.
     *
     * @param generateDdlOption
     *      generate DDL
     */
    public void generateDDL(GenerateDdlOptions generateDdlOption) {
        List<String> options = startOptions();
        options.add("generate-ddl");
        options.add(OPTION_EXPORT_USERNAME);
        options.add("token");
        options.add(OPTION_EXPORT_PASSWORD);
        options.add(CliContext.getInstance().getToken());
        options.add(OPTION_EXPORT_BUNDLE);
        Database db = getDbDao().getDatabase(generateDdlOption.getDb());
        options.add(new File(AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER + File.separator +
                AstraCliUtils.buildScbFileName(db.getId(), db.getInfo().getRegion())).getAbsolutePath());
        if (generateDdlOption.getKeyspaces() != null && !"".equals(generateDdlOption.getKeyspaces())) {
            options.add(OPTION_KEYSPACES);
            options.add(generateDdlOption.getKeyspaces());
        }
        if (generateDdlOption.getTables() != null && !"".equals(generateDdlOption.getTables())) {
            options.add(OPTION_TABLES);
            options.add(generateDdlOption.getTables());
        }
        if (generateDdlOption.getDataDir() != null && !"".equals(generateDdlOption.getDataDir())) {
            options.add(OPTION_DATA_DIRECTORY);
            options.add(generateDdlOption.getDataDir());
        }
        run(options, generateDdlOption.getDb());
    }

    /**
     * Run DSBulk.
     *
     * @param options
     *      current command line
     * @param database
     *      current db name
     */
    public void run(List<String> options, String database) {

        // Install Cqlsh for Astra and set permissions
        if (!isInstalled()) install();

        // Download scb and throw DatabaseNotFound.
        DaoDatabase.getInstance().downloadCloudSecureBundles(database);

        try {
            LoggerShell.info("RUNNING: " + String.join(" ", options));
            new ProcessBuilder(options.toArray(new String[0])).inheritIO().start().waitFor();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new CannotStartProcessException("dsbulk-migrator", e);
        }
    }

    /**
     * Syntax sugar to access db dao.
     *
     * @return
     *      db dao
     */
    private DaoDatabase getDbDao() {
        return DaoDatabase.getInstance();
    }

    /**
     * Get file Name.
     *
     * @return
     *      jar url
     */
    private File getDsBulkMigratorInstallationFolder() {
        return new File(AstraCliUtils.ASTRA_HOME
                + File.separator
                + DSBULK_MIGRATOR_PREFIX + config.version());
    }

    /**
     * Get file Name.
     *
     * @return
     *      jar url
     */
    public String getDsBulkMigratorJarLocalPath() {
        return getDsBulkMigratorInstallationFolder().getAbsolutePath() + File.separator + getDsBulkMigratorJarName();
    }

    /**
     * Get the URL.
     *
     * @return
     *      jar url
     */
    private String getDsBulkMigratorJarUrl() {
        return config.url() + config.version() + "/" + getDsBulkMigratorJarName();
    }

    /**
     * Get Jar file
     *
     * @return
     *      jar file
     */
    private String getDsBulkMigratorJarName() {
        return DSBULK_MIGRATOR_PREFIX + config.version() + "-dsbulk.jar";
    }

}
