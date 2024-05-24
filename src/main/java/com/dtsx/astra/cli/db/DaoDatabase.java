package com.dtsx.astra.cli.db;

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
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.AstraDBOpsClient;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Unitary operation for databases.
 * The singleton pattern is validated with a Lazy initialization
 * and a thread safe implementation.
 */
@SuppressWarnings("java:S6548")
public class DaoDatabase {
    
    /**
     * Singleton Pattern
     */
    private static DaoDatabase instance;
    
    /**
     * Singleton Pattern.
     * 
     * @return
     *      instance of the service.
     */
    public static synchronized DaoDatabase getInstance() {
        if (null == instance) {
            instance = new DaoDatabase();
        }
        return instance;
    }
    
    /**
     * Default Constructor.
     */
    private DaoDatabase() {
    }
    
    /**
     * Access unique db.
     * 
     * @param databaseName
     *      database name
     * @return
     *      unique db
     * @throws DatabaseNameNotUniqueException
     *      error when multiple dbs
     * @throws DatabaseNotFoundException
     *      error when db does not exist
     */
    public Database getDatabase(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Optional<Database> optDb = getRequiredDatabaseClient(databaseName).find();
        if (optDb.isPresent()) {
            return optDb.get();
        }
        throw new DatabaseNotFoundException(databaseName);
    }

    /**
     * Accessing the AstraDB client for a vectorDB.
     *
     * @param databaseName
     *      database name
     * @return
     *      astraDB
     */
    public com.datastax.astra.client.Database getDataAPIDatabase(String databaseName) {
        return getDataAPIDatabase(databaseName, AstraDBAdmin.DEFAULT_NAMESPACE);
    }

    /**
     * Accessing the AstraDB client for a vectorDB.
     *
     * @param databaseName
     *      database name
     * @return
     *      instance of a client for the DataAPI
     */
    public com.datastax.astra.client.Database getDataAPIDatabase(String databaseName, String keyspace) {
        Database db = getDatabase(databaseName);
        if (db.getInfo().getDbType() == null) {
            throw new IllegalArgumentException("Database %s is not a vector database".formatted(databaseName));
        }
        return CliContext.getInstance().getDataAPIClient().getDatabase(UUID.fromString(db.getId()), keyspace);
    }

    /**
     * Access unique db.
     * 
     * @param databaseName
     *      database name
     * @return
     *      unique db
     * @throws DatabaseNameNotUniqueException
     *      error when multiple dbs
     * @throws DatabaseNotFoundException
     *      error when db does not exist
     */
    public DbOpsClient getRequiredDatabaseClient(String databaseName) {
        Optional<DbOpsClient> dbClient = getDatabaseClient(databaseName);
        if (dbClient.isPresent()) {
            return dbClient.get();
        }
        throw new DatabaseNotFoundException(databaseName);
    }
    
    /**
     * Load the databaseClient by user input.
     * 
     * @param db
     *      database name or identifier
     * @return
     *      db id
     * @throws DatabaseNameNotUniqueException 
     *      cli does not work if multiple db with same name
     */
    public Optional<DbOpsClient> getDatabaseClient(String db)
    throws DatabaseNameNotUniqueException {
        AstraDBOpsClient dbsClient = CliContext.getInstance().getApiDevopsDatabases();
        
        // Escape special chars
        db = db.replace("\"", "");
        // Database name containing spaces cannot be an id
        if (!db.contains(" ") ) {
            DbOpsClient dbClient = dbsClient.database(db);
            if (dbClient.exist()) {
                LoggerShell.debug("Database found id=" + db);
                return Optional.of(dbClient);
            }
        }

        // Not found, try with the name
        List<Database> dbs = dbsClient.findByName(db).toList();
        
        // Multiple databases with the same name
        if (dbs.size() > 1) {
            throw new DatabaseNameNotUniqueException(db);
        }
        
        // Database exists and is unique
        if (1 == dbs.size()) {
            LoggerShell.debug("Database found id=" + dbs.get(0).getId());
            return Optional.ofNullable(dbsClient.database(dbs.get(0).getId()));
        }
        return Optional.empty();
    }
    
    /**
     * Download the cloud secure bundles.
     * 
     * @param databaseName
     *      database name and id
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     */
    public void downloadCloudSecureBundles(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        getRequiredDatabaseClient(databaseName)
            .downloadAllSecureConnectBundles(
                    AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER);
        LoggerShell.debug("Secure connect bundles have been downloaded.");
    }
    
    /**
     * Download SCB when needed.
     *
     * @param databaseName
     *      database name.
     * @param region
     *      specified region 
     * @param location
     *      location
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     * @throws InvalidArgumentException
     *      invalid argument to download sb. 
     */
    public void downloadCloudSecureBundle(String databaseName, String region, String location)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException {
        Database        db  = getDatabase(databaseName);
        Set<Datacenter> dcs = db.getInfo().getDatacenters();
        Datacenter      dc  = dcs.iterator().next();
        if (dcs.size() > 1) {
            if (null == region) {
                throw new InvalidArgumentException(
                         "Your database is deployed on multiple regions. "
                        + "A scb is associated to only one region. "
                        + "Add -r or --region in the command to select one.");
            }
            Optional<Datacenter> optDc = dcs.stream()
               .filter(d -> d.getName().equals(region)).findFirst();
            if (optDc.isEmpty()) {
                throw new InvalidArgumentException(
                        "Your database is deployed on multiple regions. "
                        + "You select and invalid region name. "
                        + "Please use one from %s".formatted(
                                dcs.stream().map(Datacenter::getName)
                                   .toList().toString()));
            }
            dc = optDc.get();
        }
        
        // Default location
        if (location == null) {
            location = "." + File.separator + "scb_" + db.getId() + "_" + dc.getName() + ".zip";
        }
        FileUtils.downloadFile(dc.getSecureBundleUrl(), location);
        LoggerShell.info("Bundle downloaded in %s".formatted(location));
    }
    

}
