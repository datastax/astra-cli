package com.dtsx.astra.cli.db.region;

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
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.db.exception.CannotCreateRegionException;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.sdk.db.AstraDBOpsClient;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.db.exception.RegionAlreadyExistException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Operation on regions
 * The singleton pattern is validated with a Lazy initialization
 * and a thread safe implementation.
 */
@SuppressWarnings("java:S6548")
public class ServiceRegion {

    /** column names. */
    public static final String REGION = "Region";

    /** column names. */
    public static final String COLUMN_CLOUD      = "Cloud Provider";

    /** column names. */
    public static final String COLUMN_REGION_NAME = "Region";

    /** column names. */
    public static final String COLUMN_REGION_STATUS = "Status";

    /** column names. */
    public static final String COLUMN_REGION_TIER = "Tier";

    /**
     * Singleton Pattern
     */
    private static ServiceRegion instance;

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
    public static synchronized ServiceRegion getInstance() {
        if (null == instance) {
            instance = new ServiceRegion();
        }
        return instance;
    }

    /**
     * Default Constructor.
     */
    private ServiceRegion() {
        this.dbDao = DaoDatabase.getInstance();
    }

    /**
     * Access Api devops from context.
     *
     * @return
     *      api devops
     */
    private AstraDBOpsClient apiDevopsDb() {
        return CliContext.getInstance().getApiDevopsDatabases();
    }

    /**
     * List keyspaces of a database.
     *
     * @param databaseName
     *      database name
     */
    public void listRegions(String databaseName) {
        Database db  = dbDao.getDatabase(databaseName);
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_CLOUD,    15);
        sht.addColumn(COLUMN_REGION_NAME,    20);
        sht.addColumn(COLUMN_REGION_TIER,    15);
        sht.addColumn(COLUMN_REGION_STATUS,    15);
        db.getInfo().getDatacenters().forEach(dc -> {
            Map<String, String> rf = new HashMap<>();
            rf.put(COLUMN_CLOUD, dc.getCloudProvider().name().toLowerCase());
            if (db.getInfo().getRegion().equals(dc.getRegion())) {
                rf.put(COLUMN_REGION_NAME, dc.getRegion() + " (default)");
            } else {
                rf.put(COLUMN_REGION_NAME, dc.getRegion());
            }
            rf.put(COLUMN_REGION_TIER, dc.getTier());
            rf.put(COLUMN_REGION_STATUS, dc.getStatus());
            try {
                System.out.println(new ObjectMapper().writeValueAsString(dc));
            } catch(Exception e) {
                e.printStackTrace();
            }

            sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Adding a region to a DB if not exists.
     *
     * @param databaseName
     *      database name
     * @param regionName
     *      region name
     * @param cloudProvider
     *      cloud provider
     * @param tier
     *      tier
     * @param ifNotExist
     *      only operate if the region does not exist
     */
    public void addRegion(String databaseName, String regionName, String cloudProvider, String tier, boolean ifNotExist) {
        Database db  = dbDao.getDatabase(databaseName);
        Set<String> regionsDb = db.getInfo()
                    .getDatacenters().stream()
                    .map(Datacenter::getRegion)
                    .collect(Collectors.toSet());
        if (regionsDb.contains(regionName)) {
            if (ifNotExist) {
                LoggerShell.info("No action as %s '%s' already exists.".formatted(REGION, regionName));
            } else {
                throw new RegionAlreadyExistException(regionName, databaseName);
            }
        } else {
            try {
                CloudProviderType cloud = db.getInfo().getCloudProvider();
                if (cloudProvider != null) cloud = CloudProviderType.valueOf(cloudProvider.toUpperCase());
                apiDevopsDb().database(db.getId()).datacenters().create(tier, cloud, regionName);
                LoggerShell.info("%s '%s' is creating.".formatted(REGION, regionName));
            } catch(Exception e) {
                LoggerShell.error("Error when creating a region : %s".formatted(e.getMessage()));
                throw new CannotCreateRegionException(databaseName, regionName, e);
            }
        }
    }

    /**
     * Loop and wait 1s in between 2 tests.
     *
     * @param dbName
     *      database name
     * @param regionName
     *      region to be deleted
     * @param timeout
     *      timeout is max retries
     * @return
     *      max retried
     */
    public int retryUntilRegionDeleted(String dbName, String regionName, int timeout) {
        int retries = 0;
        Database db = dbDao.getDatabase(dbName);
        while (((retries++ < timeout) || (timeout == 0)) &&
                apiDevopsDb().database(db.getId()).datacenters().findByRegionName(regionName).isPresent()) {
            try {
                Thread.sleep(1000);
                LoggerShell.debug("Waiting for %s to be deleted ( %d / %d )".formatted(REGION, retries, timeout));
            } catch (InterruptedException e) {
                LoggerShell.error("Interrupted operation: %s".formatted(e.getMessage()));
                Thread.currentThread().interrupt();
            }
        }
        return retries;
    }

    /**
     * Delete a region to a DB
     *
     * @param databaseName
     *      database name
     * @param regionName
     *      region name
     */
    public void deleteRegion(String databaseName, String regionName) {
        // Throw db not found  exception if database does not exist
        Database db = dbDao.getDatabase(databaseName);
        // Throw region not found exception if region does not exist
        apiDevopsDb().database(db.getId()).datacenters().delete(regionName);
        LoggerShell.info("%s '%s' is deleting.".formatted(REGION, regionName));
    }

}
