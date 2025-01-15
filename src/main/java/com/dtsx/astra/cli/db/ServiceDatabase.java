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

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.core.exception.InvalidCloudProviderException;
import com.dtsx.astra.cli.core.exception.InvalidRegionException;
import com.dtsx.astra.cli.core.out.AstraAnsiColors;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.JsonOutput;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.core.out.OutputFormat;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.core.out.StringBuilderAnsi;
import com.dtsx.astra.cli.db.exception.DatabaseAlreadyExistException;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.cli.db.keyspace.ServiceKeyspace;
import com.dtsx.astra.cli.org.ServiceOrganization;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.dtsx.astra.cli.utils.EnvFile;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationBuilder;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseRegion;
import com.dtsx.astra.sdk.db.domain.DatabaseRegionServerless;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.db.domain.RegionType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.db.exception.KeyspaceAlreadyExistException;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.Assert;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.out.AstraAnsiColors.BLUE_500;
import static com.dtsx.astra.cli.core.out.AstraAnsiColors.GREEN_500;
import static com.dtsx.astra.cli.core.out.AstraAnsiColors.NEUTRAL_500;
import static com.dtsx.astra.cli.core.out.AstraAnsiColors.RED_500;
import static com.dtsx.astra.cli.core.out.AstraAnsiColors.YELLOW_300;
import static com.dtsx.astra.cli.core.out.AstraAnsiColors.YELLOW_500;

/**
 * Service grouping all operations on Astra Db Databases.
 * The singleton pattern is validated with a Lazy initialization
 * and a thread safe implementation.
 */
@SuppressWarnings("java:S6548")
public class ServiceDatabase {

    /** Default timeout for operations. */
    public static final int DEFAULT_TIMEOUT_SECONDS = 400;

    /** Default timeout for connection. */
    public static final int CONNECT_TIMEOUT_SECONDS = 20;
    
    /** column names. */
    static final String COLUMN_ID                = "id";
    /** column names. */
    static final String COLUMN_NAME              = "Name";
    /** column names. */
    static final String COLUMN_VECTOR_SEARCH     = "V";
    /** column names. */
    static final String COLUMN_REGIONS           = "Regions";
    /** column names. */
    static final String COLUMN_DEFAULT_CLOUD     = "Cloud";
    /** column status. */
    static final String COLUMN_STATUS            = "Status";
    /** column status. */
    static final int    COLUMN_STATUS_WIDTH      = 10;
    /** column names. */
    static final String COLUMN_DEFAULT_KEYSPACE  = "Default Keyspace";
    /** column names. */
    static final String COLUMN_KEYSPACES         = "Keyspaces";
    /** working object. */
    static final String DB                       = "Database";
    /** working object. */
    static final String COLUMN_VECTOR            = "Vector";

    /**
     * Access to databases object.
     */
    DaoDatabase dbDao;

    /**
     * JDK HttpClient
     */
    HttpClient client;
    
    /**
     * Singleton Pattern
     */
    private static ServiceDatabase instance;

    /**
     * Singleton Pattern.
     * 
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceDatabase getInstance() {
        if (null == instance) {
            instance = new ServiceDatabase();
        }
        return instance;
    }
    
    /**
     * Default Constructor.
     */
    private ServiceDatabase() {
        this.dbDao = DaoDatabase.getInstance();
        client = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Wait for a DB status.
     *
     * @param databaseName
     *      database name
     * @param status
     *      expected status
     * @param timeout
     *      timeout number of loop to wait
     * @return
     *      db is in correct status
     * @throws DatabaseNameNotUniqueException
     *      db name is present multiple times
     * @throws DatabaseNotFoundException
     *      database has not been found
     */
    public ExitCode waitForDbStatus(String databaseName, DatabaseStatusType status, int timeout)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Optional<DbOpsClient> dbClient = dbDao.getDatabaseClient(databaseName);
        if (dbClient.isPresent()) {
           DbOpsClient     dbc   = dbClient.get();
           Optional<Database> optDb = dbc.find();
           if (optDb.isPresent()) {
               Database db = optDb.get();
               if (db.getStatus().equals(status)) {
                   return ExitCode.SUCCESS;
               }
               LoggerShell.info("%s '%s' has status '%s' waiting to be '%s' ..."
                       .formatted(DB, databaseName, db.getStatus(), status));
               
               long start = System.currentTimeMillis();
               return evalReturnCode(db, status, retryUntilTimeoutOrSuccess(db, status, timeout), timeout, start);
            }
        }
        LoggerShell.error("%s '%s' has not been found."
                .formatted(DB, databaseName));
        return ExitCode.NOT_FOUND;
    }
    
    /**
     * Based on computation result give us a return code.
     * 
     * @param db
     *      current db
     * @param status
     *      expected status
     * @param retries
     *      number of retries
     * @param timeout
     *      timeout
     * @param start
     *      start time
     * @return
     *      exit code
     */
    public ExitCode evalReturnCode(Database db, DatabaseStatusType status, int retries, int timeout, long start) {
        
        // Success if you did not reach the timeout (meaning status is good)
        if (retries < timeout) {
            LoggerShell.info("%s '%s' has status '%s' (took %d millis)"
                       .formatted(DB, db.getInfo().getName(), status, 
                               System.currentTimeMillis() - start));
            return ExitCode.SUCCESS;
        }
        
        LoggerShell.warning("Timeout (%d s) : %s '%s' status is not yet '%s' (current status '%s')"
                       .formatted(timeout, DB, db.getInfo().getName(), 
                        status.toString(), db.getStatus().toString()));
        return ExitCode.UNAVAILABLE;
    }
    
    /**
     * Loop and wait 1s in between 2 tests.
     * 
     * @param db
     *      database
     * @param status
     *      current status
     * @param timeout
     *      timeout is max retries
     * @return
     *      max retried
     */
    public int retryUntilTimeoutOrSuccess(Database db, DatabaseStatusType status, int timeout) {
        int retries = 0;
        while (((timeout == 0) || (retries++ < timeout)) && !db.getStatus().equals(status)) {
            try {
             Thread.sleep(1000);
             db = dbDao.getDatabase(db.getInfo().getName());
             LoggerShell.debug("Waiting for %s to become '%s' but was '%s' retrying ( %d / %d )"
                     .formatted(DB, status.toString(), db.getStatus().toString(), retries, timeout));
            } catch (InterruptedException e) {
                LoggerShell.error("Interrupted operation: %s".formatted(e.getMessage()));
                Thread.currentThread().interrupt();
            }
        }
        return retries;
    }

    /**
     * Loop and wait 1s in between 2 tests.
     *
     * @param dbName
     *      database name
     * @param timeout
     *      timeout is max retries
     * @return
     *      max retried
     */
    public int retryUntilDbDeleted(String dbName, int timeout) {
        int retries = 0;
        Optional<DbOpsClient> optDbClient = dbDao.getDatabaseClient(dbName);
        while (retries++ < timeout && optDbClient.isPresent()) {
            try {
                Thread.sleep(1000);
                optDbClient = dbDao.getDatabaseClient(dbName);
                LoggerShell.debug("Waiting for %s to be deleted ( %d / %d )"
                        .formatted(DB, retries, timeout));
            } catch (InterruptedException e) {
                LoggerShell.error("Interrupted operation: %s".formatted(e.getMessage()));
                Thread.currentThread().interrupt();
            }
        }
        return retries;
    }

    /**
     * Validate that provided region is in the target cloud.
     *
     * @param flagVector
     *      enable flag for vector
     * @param cloud
     *      provided cloud
     * @param region
     *      provided region
     */
    public void validateCloudAndRegion(String cloud, String region, boolean flagVector) {
        Assert.hasLength(region, "region name");
        if (!StringUtils.isEmpty(cloud)) {
            SortedMap<String, TreeMap<String, String>> mapCloudRegions =
                    ServiceOrganization.getInstance()
                            .getDbServerlessRegions(flagVector ? RegionType.VECTOR : RegionType.ALL);
            if (!mapCloudRegions.containsKey(cloud.toLowerCase())) {
                throw new InvalidCloudProviderException(cloud);
            } else if (!mapCloudRegions
                    .get(cloud.toLowerCase())
                    .containsKey(region.toLowerCase())) {
                throw new InvalidRegionException(cloud, region);
            }
        } else if (CliContext.getInstance()
                .getApiDevops().db().regions()
                .findAllServerless(flagVector ? RegionType.VECTOR : RegionType.ALL)
                .map(DatabaseRegionServerless::getName)
                .filter(r -> r.equals(region.toLowerCase()))
                .findFirst().isEmpty()) {
            throw new InvalidRegionException(region);
        }
    }

    /**
     * Create a new database
     * 
     * @param options
     *      Database creation options
     * @throws DatabaseAlreadyExistException
     *      db name already exist
     * @throws InvalidArgumentException
     *      error in params 
     * @throws DatabaseNotFoundException
     *      error when db not found (when creating keyspace)
     * @throws KeyspaceAlreadyExistException
     *      keyspace already exist for this db
     * @throws InvalidDatabaseStateException
     *      database is hibernating or error state, cannot proceed 
     */
    public void createDb(DbCreationOptions options)
    throws DatabaseAlreadyExistException, DatabaseNotFoundException,
           InvalidDatabaseStateException, InvalidArgumentException, KeyspaceAlreadyExistException {
        validateNames(options.databaseName(), options.keyspaceName());
        // if multiple databases with same name => error
        try {
            Optional<DbOpsClient> dbClient = dbDao.getDatabaseClient(options.databaseName());
            if (dbClient.isEmpty()) {
                createNewDb(options);
            } else {
                if (!options.flagIfNotExist()) {
                    throw new DatabaseAlreadyExistException(options.databaseName());
                }
                Database db = dbDao.getDatabase(options.databaseName());
                DatabaseStatusType dbStatus = db.getStatus();
                switch (dbStatus) {
                    case HIBERNATED -> {
                        resumeDb(options.databaseName());
                        waitForDbStatus(options.databaseName(), DatabaseStatusType.ACTIVE, DEFAULT_TIMEOUT_SECONDS);
                        dbStatus = dbDao.getDatabase(options.databaseName()).getStatus();
                    }
                    case PENDING, RESUMING, INITIALIZING, MAINTENANCE -> {
                        waitForDbStatus(options.databaseName(), DatabaseStatusType.ACTIVE, DEFAULT_TIMEOUT_SECONDS);
                        dbStatus = dbDao.getDatabase(options.databaseName()).getStatus();
                    }
                    default -> LoggerShell.info("%s '%s' already exist. Connecting to database.".formatted(DB, options.databaseName()));
                }

                // Create keyspace on existing DB when needed
                if (DatabaseStatusType.ACTIVE.equals(dbStatus)) {
                    ServiceKeyspace.getInstance().createKeyspace(options.databaseName(), options.keyspaceName(), true);
                } else {
                    throw new InvalidDatabaseStateException(options.databaseName(), DatabaseStatusType.ACTIVE, dbStatus);
                }
            }
        } catch (DatabaseNameNotUniqueException ex) {
            throw new DatabaseAlreadyExistException(options.databaseName());
        }
    }

    /**
     * Create a new Database with provided parameters.
     *
     * @param options
     *      database creation options
     */
    private void createNewDb(DbCreationOptions options) {
        LoggerShell.info("%s '%s' does not exist. Creating database '%s' with keyspace '%s'"
                .formatted(DB, options.databaseName(), options.databaseName(), options.keyspaceName()));
        DatabaseCreationBuilder builder = DatabaseCreationRequest.builder()
                .name(options.databaseName())
                .tier(options.tier())
                .capacityUnit(options.capacityUnits())
                .cloudProvider(getCloudProvider(options.tier(), options.databaseRegion(), options.flagVector()))
                .cloudRegion(options.databaseRegion())
                .keyspace(options.keyspaceName());
        if (options.flagVector()) {
            LoggerShell.info("Enabling vector search for database %s".formatted(options.databaseName()));
            builder.withVector();
        }
        CliContext.getInstance()
                    .getApiDevopsDatabases()
                    .create(builder.build());
        LoggerShell.info("%s '%s' and keyspace '%s' are being created."
                .formatted(DB, options.databaseName(), options.keyspaceName()));
    }

    /**
     * Validate Keyspace and database names
     * @param databaseName
     *      database name
     * @param keyspaceName
     *      keyspace name
     */
    private void validateNames(String databaseName, String keyspaceName) {
        if (!keyspaceName.matches(ServiceKeyspace.KEYSPACE_NAME_PATTERN)) {
            throw new InvalidArgumentException("Keyspace should contain alphanumerics[a-z0-9_]");
        }
        if (databaseName.length() < 3 || databaseName.length() > 50) {
            throw new InvalidArgumentException("Database name '" + databaseName + "' should have between 2 and 50 characters");
        }
    }

    /**
     * Get serverless region with dedup.
     *
     * @param flagVector
     *      vector
     * @return
     *      dedup map
     */
    private Map<String, DatabaseRegionServerless> getRegionMapServerless(boolean flagVector) {
        return CliContext.getInstance()
                .getApiDevops()
                .db().regions()
                .findAllServerless(flagVector ? RegionType.VECTOR : RegionType.ALL)
                .collect(Collectors.toMap(DatabaseRegionServerless::getName, Function.identity(), (a, b) -> a));
    }

    /**
     * Access the cloud provider from region name.
     *
     * @param tier
     *      adapting control with tier
     * @param databaseRegion
     *      database region
     * @param flagVector
     *      will only list supported region for vector
     * @return
     *      cloud provider or error
     */
    private CloudProviderType getCloudProvider(String tier, String databaseRegion, boolean flagVector) {
        // SERVERLESS
        if (DatabaseCreationBuilder.DEFAULT_TIER.equals(tier)) {
            Map<String, DatabaseRegionServerless> regionMap = getRegionMapServerless(flagVector);
            if (!regionMap.containsKey(databaseRegion)) {
                throw new InvalidArgumentException("Region '" + databaseRegion + "' has not been found for serverless");
            }
            return CloudProviderType.valueOf(regionMap
                    .get(databaseRegion)
                    .getCloudProvider()
                    .toUpperCase());
        } else {
            // Used to deduplicate regions, some have same keys (?!)
            Map<String, DatabaseRegion> regionMap = new HashMap<>();
            for (DatabaseRegion db : CliContext.getInstance()
                    .getApiDevops().db().regions()
                    .findAll().toList()) {
                regionMap.put(db.getRegion(), db);
            }
            if (!regionMap.containsKey(databaseRegion)) {
                throw new InvalidArgumentException("Database region '" + databaseRegion + "' has not been found for classic");
            }
            return regionMap.get(databaseRegion).getCloudProvider();
        }
    }

    /**
     * List Databases.
     *
     * @param flagVector
     *      only show the databases vector
     */
    public void listDb(boolean flagVector) {
        ShellTable sht = new ShellTable();

        // No color ?
        sht.addColumn(COLUMN_NAME,    20);
        sht.addColumn(COLUMN_ID,      37);
        sht.addColumn(COLUMN_REGIONS, 10);
        sht.addColumn(COLUMN_DEFAULT_CLOUD, 6);
        sht.addColumn(COLUMN_VECTOR_SEARCH, 2);
        sht.addColumn(COLUMN_STATUS,  COLUMN_STATUS_WIDTH);
        CliContext.getInstance()
           .getApiDevopsDatabases()
           .findAllNonTerminated()
           .filter(db -> !flagVector || (db.getInfo().getDbType() != null))
           .forEach(db -> {
                Map <String, String> rf = new HashMap<>();
                db.getInfo().getDatacenters().forEach(dc -> rf.put(dc.getRegion(), dc.getRegion()));
                rf.put(COLUMN_NAME,    db.getInfo().getName());
                rf.put(COLUMN_ID,      db.getId());
                rf.put(COLUMN_REGIONS, db.getInfo().getRegion());
                rf.put(COLUMN_DEFAULT_CLOUD, db.getInfo().getCloudProvider().toString().toLowerCase());
                rf.put(COLUMN_VECTOR_SEARCH, "");
                if (db.getInfo().getDbType() != null) {
                    rf.put(COLUMN_VECTOR_SEARCH, "■");
                }
                String status = db.getStatus().name();
                // Colored if displayed as table
                if (CliContext.getInstance().getOutputFormat().equals(OutputFormat.HUMAN)) {
                    status = StringBuilderAnsi.colored(status, getStatusColor(db.getStatus()));
                }
                rf.put(COLUMN_STATUS, status);
                sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Utility to color the status based on the value.
     *
     * @param status
     *      current db status
     * @return
     *      colored status
     */
    private AstraAnsiColors getStatusColor(DatabaseStatusType status) {
        AstraAnsiColors color = NEUTRAL_500;
        switch (status) {
            // Active is Green
            case ACTIVE -> color = GREEN_500;
            // Error is RED
            case ERROR, TERMINATED, UNKNOWN  -> color = RED_500;
            // Going into error is Yellow
            case DECOMMISSIONING, TERMINATING, DEGRADED -> color = YELLOW_500;
            // Dormant is blue
            case HIBERNATED, PARKED, PREPARED ->  color = BLUE_500;
            // Temporary states back to active are cyan
            case INITIALIZING, PENDING, HIBERNATING, PARKING, MAINTENANCE,
                 PREPARING, RESIZING, RESUMING, UNPARKING -> color = YELLOW_300;
        }
        return color;
    }
    
    /**
     * Delete a database if exist.
     * 
     * @param databaseName
     *      db name or db id
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     */
    public void deleteDb(String databaseName) 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        dbDao.getRequiredDatabaseClient(databaseName).delete();
        LoggerShell.info("Deleting Database '%s'".formatted(databaseName));
    }
  
    /**
     * Resume a database if exist.
     * 
     * @param databaseName
     *      db name or db id
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     * @throws InvalidDatabaseStateException
     *      database is in invalid state
     */
    public void resumeDb(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, 
           InvalidDatabaseStateException {
        Database db = dbDao.getDatabase(databaseName);
        switch (db.getStatus()) {
            case ACTIVE ->
                    LoggerShell.info("%s '%s' is already active".formatted(DB, databaseName));
            case HIBERNATED -> {
                resumeDbRequest(db);
                LoggerShell.success("Resuming %s '%s' ...".formatted(DB, databaseName));
            }
            case MAINTENANCE,INITIALIZING,PENDING ->
                    LoggerShell.info("%s '%s' will be available soon".formatted(DB, databaseName));
            case RESUMING ->
                    LoggerShell.info("%s '%s is already resuming".formatted(DB, databaseName));
            default -> throw new InvalidDatabaseStateException(databaseName, DatabaseStatusType.HIBERNATED, db.getStatus());
        }
    }
    
    /**
     * Database name.
     *
     * @param db
     *      database name
     */
    private void resumeDbRequest(Database db) {
        try {
            // Compute Endpoint for the Keyspaces
            String endpoint = ApiLocator.getApiRestEndpoint(db.getId(), db.getInfo().getRegion()) +
                    "/v2/schemas/keyspace";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("X-Cassandra-Token", CliContext.getInstance().getToken())
                    .GET()
                    .build();
            
             HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
             if (response.statusCode() == 500)
                 throw new InvalidDatabaseStateException("Cannot resume db error: %s".formatted(response.body()));
        } catch (InterruptedException e) {
            LoggerShell.warning("Interrupted %s".formatted(e.getMessage()));
            Thread.currentThread().interrupt();
        } catch (Exception e) {
             LoggerShell.warning("Resuming request might have failed, please check %s".formatted(e.getMessage()));
        }
    }
    
    /**
     * Display status of a database.
     * 
     * @param databaseName
     *      database name
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     */
    public void showDbStatus(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        AstraCliConsole.outputSuccess("Database '%s' has status '%s'"
                    .formatted(databaseName, dbDao.getDatabase(databaseName).getStatus()));
    }
    
    /**
     * Show database details.
     *
     * @param databaseName
     *      database name and id
     * @param key
     *      show only a key     
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     */
    public void showDb(String databaseName, DbGetCmd.DbGetKeys key)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Database db = dbDao.getDatabase(databaseName);
        if (key == null) {
            ShellTable sht = ShellTable.propertyTable(15, 40);
            sht.addPropertyRow(COLUMN_NAME, db.getInfo().getName());
            sht.addPropertyRow(COLUMN_ID, db.getId());
            sht.addPropertyRow(COLUMN_DEFAULT_CLOUD, db.getInfo().getCloudProvider().name());
            sht.addPropertyRow(COLUMN_REGIONS, db.getInfo().getRegion());
            sht.addPropertyRow(COLUMN_STATUS, db.getStatus().toString());
            sht.addPropertyRow(COLUMN_VECTOR, db.getInfo().getDbType() != null ? "Enabled" : "Disabled");
            sht.addPropertyRow(COLUMN_DEFAULT_KEYSPACE, db.getInfo().getKeyspace());
            sht.addPropertyRow("Creation Time", db.getCreationTime());
            List<String> regions   = db.getInfo().getDatacenters()
                    .stream()
                    .map(Datacenter::getRegion)
                    .toList();
            List<String> keyspaces = new ArrayList<>(db.getInfo().getKeyspaces());
            switch (CliContext.getInstance().getOutputFormat()) {
                case CSV -> {
                    sht.addPropertyRow(COLUMN_REGIONS, regions.toString());
                    sht.addPropertyRow(COLUMN_KEYSPACES, keyspaces.toString());
                    AstraCliConsole.printShellTable(sht);
                }
                case JSON -> AstraCliConsole.printJson(new JsonOutput<>(ExitCode.SUCCESS,
                        "db get %s".formatted(databaseName), db));
                case HUMAN -> {
                    sht.addPropertyListRows(COLUMN_KEYSPACES, keyspaces);
                    sht.addPropertyListRows(COLUMN_REGIONS, regions);
                    AstraCliConsole.printShellTable(sht);
                }
            }
         } else {
            switch (key) {
                case ID -> AstraCliConsole.println(db.getId());
                case CLOUD -> AstraCliConsole.println(db.getInfo().getCloudProvider().name());
                case KEYSPACE -> AstraCliConsole.println(db.getInfo().getKeyspace());
                case KEYSPACES -> AstraCliConsole.println(new ArrayList<>(db.getInfo().getKeyspaces()).toString());
                case REGION -> AstraCliConsole.println(db.getInfo().getRegion());
                case REGIONS -> AstraCliConsole.println(db.getInfo().getDatacenters()
                        .stream()
                        .map(Datacenter::getRegion)
                        .toList()
                        .toString());
                case STATUS -> AstraCliConsole.println(db.getStatus().toString());
            }
            
         }
    }

    /**
     * Retrieve region name.
     *
     * @param region
     *      forced region name
     * @return
     *      region name
     */
    private String retrieveDatabaseRegion(Database db, String region) {

        if (region == null) {
            region = db.getInfo().getRegion();
        }

        // Db is actually present in that region
        Map<String, Datacenter> datacenters = db
                .getInfo().getDatacenters()
                .stream().collect(Collectors.toMap(Datacenter::getRegion, Function.identity()));
        if (!datacenters.containsKey(region)) {
            throw new InvalidArgumentException("Region %s is not part of existing regions %s, use flag -r to specify region name"
                    .formatted(region, datacenters.keySet().toString()));
        }

        return region;
    }
    
    /**
     * Generation configuration File.
     * 
     * @param dbName
     *      database name or identifier
     * @param ks
     *      keyspace
     * @param region
     *      region
     * @param dest
     *      destinations
     * @throws DatabaseNotFoundException 
     *      error db not found
     * @throws DatabaseNameNotUniqueException
     *      error db not unique
     * @throws InvalidArgumentException
     *      invalid argument
     */
    public void generateDotEnvFile(String dbName, String ks, String region, String dest) 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException {
        EnvFile envFile = new EnvFile(dest);

        // Organization Block
        Organization org = CliContext.getInstance().getApiDevops().getOrganization();
        AstraEnvironment astraEnvironment = CliContext.getInstance().getAstraEnvironment();
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_ORG_ID.name(), org.getId());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_ORG_NAME.name(), org.getName());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_ORG_TOKEN.name(), CliContext.getInstance().getToken());
        
        // Database
        Database db = dbDao.getDatabase(dbName);
        region = retrieveDatabaseRegion(db, region);
        Map<String, Datacenter> datacenters = db
                .getInfo().getDatacenters()
                .stream().collect(Collectors.toMap(Datacenter::getRegion, Function.identity()));
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_ID.name(), db.getId());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_REGION.name(), db.getInfo().getRegion());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_SECURE_BUNDLE_URL.name(), datacenters.get(region).getSecureBundleUrl());

        Set <String> dbRegions = db.getInfo().getDatacenters()
                .stream().map(Datacenter::getRegion)
                .collect(Collectors.toSet());
        if (!dbRegions.contains(region)) {
            throw new InvalidArgumentException("Database is not deployed in provided region");
        }
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_REGION.name(), region);

        // Application Token
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_APPLICATION_TOKEN.name(), CliContext.getInstance().getToken());
        // Cloud secure Bundle
        dbDao.downloadCloudSecureBundles(dbName);
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_SECURE_BUNDLE_PATH.name(), AstraCliUtils.ASTRA_HOME
                + File.separator + AstraCliUtils.SCB_FOLDER + File.separator
                + "scb_" + db.getId() + "_" + region + ".zip");
        // GraphQL URL
        String graphQLEndpoint = getEndpointGraphQL(dbName, region);
        // Keyspace
        if (ks == null) ks = db.getInfo().getKeyspace();
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_KEYSPACE.name(), ks);
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_GRAPHQL_URL.name(), graphQLEndpoint + "/graphql/" + ks);
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_GRAPHQL_URL_PLAYGROUND.name(), graphQLEndpoint + "/playground");
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_GRAPHQL_URL_SCHEMA.name(), graphQLEndpoint + "/graphql-schema");
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_GRAPHQL_URL_ADMIN.name(), graphQLEndpoint + "/graphql-admin");

        // Data API
        boolean isVectorDB = (db.getInfo().getDbType() != null);
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_API_ENDPOINT.name(),
                isVectorDB ? getEndpointDataAPI(dbName, region) : "");
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_API_ENDPOINT_SWAGGER.name(),
                isVectorDB ? getEndpointSwagger(dbName, region) : "");

        // Rest URL
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_REST_URL.name(),
                isVectorDB ? "" :  getEndpointAPI(dbName, region));
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_DB_REST_URL_SWAGGER.name(),
                isVectorDB ? "" : getEndpointSwagger(dbName, region));

        envFile.save();
    }

    /**
     * Build Swagger Url based on db and region.
     *
     * @param dbName
     *      database name
     * @param region
     *      database region
     * @return
     *      swagger url
     */
    public String getEndpointRest(String dbName, String region) {
        Database db = dbDao.getDatabase(dbName);
        if (db.getInfo().getDbType() != null) {
            throw new InvalidArgumentException("Vector database '%s' does not provide Cassandra Rest API".formatted(dbName));
        }
        return ApiLocator.getApiRestEndpoint(
                CliContext.getInstance().getAstraEnvironment(),
                db.getId(),
                retrieveDatabaseRegion(db, region));
    }

    /**
     * Build Swagger Url based on db and region.
     *
     * @param dbName
     *      database name
     * @param region
     *      database region
     * @return
     *      swagger url
     */
    public String getEndpointSwagger(String dbName, String region) {
        Database db = dbDao.getDatabase(dbName);
        if (db.getInfo().getDbType() == null) {
            return getEndpointRest(dbName, region) + "/swagger-ui/";
        }
        return getEndpointDataAPI(dbName, region) + "/api/json/swagger-ui/";
    }

    /**
     * Build Swagger Url based on db and region.
     *
     * @param dbName
     *      database name
     * @param region
     *      database region
     * @return
     *      swagger url
     */
    public String getEndpointDataAPI(String dbName, String region) {
        Database db = dbDao.getDatabase(dbName);
        if (db.getInfo().getDbType() == null) {
            throw new InvalidArgumentException("Database '%s' does not have vector search enabled".formatted(dbName));
        }
        return ApiLocator.getApiEndpoint(
                CliContext.getInstance().getAstraEnvironment(),
                db.getId(),
                retrieveDatabaseRegion(db, region));
    }

    /**
     * Build Swagger Url based on db and region.
     *
     * @param dbName
     *      database name
     * @param region
     *      database region
     * @return
     *      swagger url
     */
    public String getEndpointAPI(String dbName, String region) {
        Database db = dbDao.getDatabase(dbName);
        return (db.getInfo().getDbType() == null) ?
                getEndpointRest(dbName, region) :
                getEndpointDataAPI(dbName, region);
    }


    /**
     * Build Swagger Url based on db and region.
     *
     * @param dbName
     *      database name
     * @param region
     *      database region
     * @return
     *      swagger url
     */
    public String getEndpointGraphQL(String dbName, String region) {
        Database db = dbDao.getDatabase(dbName);
        return ApiLocator.getApiGraphQLEndPoint(db.getId(), retrieveDatabaseRegion(db, region));
    }

    /**
     * Build Playground Url based on db and region.
     *
     * @param dbName
     *      database name
     * @param region
     *      database region
     * @return
     *      swagger url
     */
    public String getEndpointPlayground(String dbName, String region) {
        return getEndpointGraphQL(dbName, region) +  "/playground";
    }


}
 
