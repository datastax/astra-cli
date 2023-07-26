package com.dtsx.astra.cli.db.cdc;

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
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.core.out.StringBuilderAnsi;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.sdk.db.AstraDbClient;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;

import java.util.HashMap;
import java.util.Map;

import static com.dtsx.astra.cli.core.out.AstraAnsiColors.GREEN_500;
import static com.dtsx.astra.cli.core.out.AstraAnsiColors.RED_500;
import static com.dtsx.astra.cli.core.out.AstraAnsiColors.YELLOW_500;


/**
 * Group Operations relative to Cdc.
 */
public class ServiceCdc {

    /** column names. */
    static final String COLUMN_ID               = "id";
    /** columns. */
    public static final String COLUMN_CLUSTER   = "cluster";
    /** columns. */
    public static final String COLUMN_NAMESPACE = "namespace";
    /** columns. */
    public static final String COLUMN_TENANT    = "tenant";
    /** columns. */
    public static final String COLUMN_KEYSPACE  = "keyspace";
    /** columns. */
    public static final String COLUMN_TABLE     = "table";
    /** column names. */
    static final String COLUMN_STATUS           = "Status";
    /** db status from API. */
    static final String STATUS_ERROR           = "Error";
    /** db status from API. */
    static final String STATUS_ACTIVE           = "Active";
    /** db status from API. */
    static final String STATUS_RUNNING          = "Running";

    /**
     * Singleton Pattern
     */
    private static ServiceCdc instance;

    /**
     * Access to databases object.
     */
    private final DaoDatabase dbDao;

    /**
     * Singleton Pattern.
     *
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceCdc getInstance() {
        if (null == instance) {
            instance = new ServiceCdc();
        }
        return instance;
    }

    /**
     * Default Constructor.
     */
    private ServiceCdc() {
        this.dbDao = DaoDatabase.getInstance();
    }

    /**
     * Access Api devops from context.
     *
     * @return
     *      api devops
     */
    private AstraDbClient apiDevopsDb() {
        return CliContext.getInstance().getApiDevopsDatabases();
    }

    /**
     * List Change Data Capture.
     *
     * @param databaseName
     *      database name
     */
    public void listCdc(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_ID,  20);
        sht.addColumn(COLUMN_KEYSPACE,  12);
        sht.addColumn(COLUMN_TABLE,  12);
        sht.addColumn(COLUMN_TENANT, 15);
        sht.addColumn(COLUMN_CLUSTER,   15);
        sht.addColumn(COLUMN_NAMESPACE,  15);
        sht.addColumn(COLUMN_STATUS,  15);
        dbDao.getRequiredDatabaseClient(databaseName)
                .cdc()
                .findAll()
                .forEach(cdc -> {
                    Map<String, String> rf = new HashMap<>();
                    rf.put(COLUMN_ID, cdc.getConnectorName());
                    rf.put(COLUMN_KEYSPACE, cdc.getKeyspace());
                    rf.put(COLUMN_TABLE, cdc.getDatabaseTable());
                    rf.put(COLUMN_CLUSTER,   cdc.getClusterName());
                    rf.put(COLUMN_NAMESPACE,  cdc.getNamespace());
                    rf.put(COLUMN_TENANT, cdc.getTenant());
                    rf.put(COLUMN_STATUS, getStatus(cdc));
                    sht.getCellValues().add(rf);
                });
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Utility to color the status based on the value.
     *
     * @param cdc
     *      cdc definition
     * @return
     *      colored status
     */
    private String getStatus(CdcDefinition cdc) {
        if (cdc.getCodStatus().startsWith(STATUS_ERROR)) {
            if (CliContext.getInstance().isNoColor()) {
                return STATUS_ERROR;
            } else {
                return StringBuilderAnsi.colored(STATUS_ERROR, RED_500);
            }
        } else if  (cdc.getCodStatus().equals(STATUS_ACTIVE)) {
            if (CliContext.getInstance().isNoColor()) {
                return STATUS_RUNNING;
            } else {
                return StringBuilderAnsi.colored(STATUS_RUNNING, GREEN_500);
            }
        }
        if (CliContext.getInstance().isNoColor()) {
            return cdc.getCodStatus();
        } else {
            return StringBuilderAnsi.colored( cdc.getCodStatus(), YELLOW_500);
        }
    }

    /**
     * List Change Data Capture.
     *
     * @param databaseName
     *      database name
     * @param cdcId
     *      change data capture identifier
     */
    public void deleteCdcById(String databaseName, String cdcId) {
        // Throw db not found  exception if database does not exist
        Database db = dbDao.getDatabase(databaseName);
        // Throw cdc not found exception if cdc does not exist
        apiDevopsDb().database(db.getId()).cdc().delete(cdcId);
        LoggerShell.info("Cdc '%s' from db '%s' has been deleted.".formatted(cdcId, databaseName));
    }

    /**
     * Delete a cdc providing definition.
     *
     * @param databaseName
     *      database name
     * @param keyspace
     *      keyspace name
     * @param table
     *      table name
     * @param tenant
     *      tenant name
     */
    public void deleteCdcByDefinition(String databaseName, String keyspace, String table, String tenant) {
        apiDevopsDb().database(dbDao.getDatabase(databaseName).getId()).cdc().delete(keyspace, table, tenant);
        LoggerShell.info("Cdc from db '%s' is deleting.".formatted(databaseName));
    }

    /**
     * Create a cdc from its definition.
     *
     * @param databaseName
     *      database name
     * @param keyspace
     *      keyspace name
     * @param table
     *      table name
     * @param tenant
     *      tenant name
     * @param topicPartition
     *      number of partitions in topic
     */
    public void createCdc(String databaseName, String keyspace, String table, String tenant, int topicPartition) {
        apiDevopsDb()
                .database(dbDao.getDatabase(databaseName).getId())
                .cdc()
                .create(keyspace,table, tenant, topicPartition );
        LoggerShell.info("Creating from db '%s' is creating.".formatted(databaseName));
    }

}
