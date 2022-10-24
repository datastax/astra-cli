package com.datastax.astra.cli.db;

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.JsonOutput;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.out.ShellTable;
import com.datastax.astra.cli.db.DbGetCmd.DbGetKeys;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.InvalidDatabaseStateException;
import com.datastax.astra.cli.db.exception.KeyspaceAlreadyExistException;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.datastax.astra.cli.utils.EnvFile;
import com.datastax.astra.cli.utils.EnvFile.EnvKey;
import com.datastax.astra.sdk.config.AstraClientConfig;
import com.datastax.astra.sdk.databases.DatabaseClient;
import com.datastax.astra.sdk.databases.domain.*;
import com.datastax.astra.sdk.organizations.domain.Organization;
import com.datastax.astra.sdk.utils.ApiLocator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service layer to work with database.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabaseService {
    
    /** Default region. **/
    public static final String DEFAULT_REGION        = "us-east-1";
    
    /** Default tier. **/
    public static final String DEFAULT_TIER          = "serverless";
    
    /** Allow Snake case. */
    public static final String KEYSPACE_NAME_PATTERN = "^[_a-z0-9]+$";

    /** Default timeout for operations. */
    public static final int DEFAULT_TIMEOUT_SECONDS = 300;
    
    /** column names. */
    static final String COLUMN_ID                = "id";
    /** column names. */
    static final String COLUMN_NAME              = "Name";
    /** column names. */
    static final String COLUMN_DEFAULT_REGION    = "Default Region";
    /** column names. */
    static final String COLUMN_REGIONS           = "Regions";
    /** column names. */
    static final String COLUMN_DEFAULT_CLOUD     = "Default Cloud Provider";
    /** column names. */
    static final String COLUMN_STATUS            = "Status";
    /** column names. */
    static final String COLUMN_DEFAULT_KEYSPACE  = "Default Keyspace";
    /** column names. */
    static final String COLUMN_KEYSPACES         = "Keyspaces";
    /** working object. */
    static final String DB                       = "Database";
    /** working object. */
    static final String KS                       = "Keyspace ";

    /**
     * Access to databases object.
     */
    DatabaseDao dbDao;

    /**
     * JDK 11 HttpClient
     */
    HttpClient client;
    
    /**
     * Singleton Pattern
     */
    private static DatabaseService instance;
    
    /**
     * Singleton Pattern.
     * 
     * @return
     *      instance of the service.
     */
    public static synchronized DatabaseService getInstance() {
        if (null == instance) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    /**
     * Default Constructor.
     */
    private DatabaseService() {
        this.dbDao = DatabaseDao.getInstance();
        client = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
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
        Optional<DatabaseClient> dbClient = dbDao.getDatabaseClient(databaseName);
        if (dbClient.isPresent()) {
           DatabaseClient     dbc   = dbClient.get();
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
        while (retries++ < timeout && !db.getStatus().equals(status)) {
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
        Optional<DatabaseClient> optDbClient = dbDao.getDatabaseClient(dbName);
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
     * Create a new database
     * 
     * @param databaseName
     *      db name
     * @param databaseRegion
     *      db region
     * @param keyspace
     *      db ks
     * @param ifNotExist
     *      will create if needed
     * @throws DatabaseNameNotUniqueException 
     *      db name not unique
     * @throws InvalidArgumentException
     *      error in params 
     * @throws DatabaseNotFoundException
     *      error when db not found (when creating keyspace)
     * @throws KeyspaceAlreadyExistException
     *      keyspace already exist for this db
     * @throws InvalidDatabaseStateException
     *      database is hibernating or error state, cannot proceed 
     */
    public void createDb(String databaseName, String databaseRegion, String keyspace, boolean ifNotExist) 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, 
           InvalidDatabaseStateException, InvalidArgumentException, 
           KeyspaceAlreadyExistException {
        
        // Parameter Validations
        Map<String, DatabaseRegionServerless> regionMap = CliContext.getInstance()
                .getApiDevopsOrganizations()
                .regionsServerless()
                .collect(Collectors
                .toMap(DatabaseRegionServerless::getName, Function.identity()));
        if (StringUtils.isEmpty(keyspace)) {
            keyspace = databaseName.toLowerCase()
                    .replace(" ", "_")
                    .replace("-", "_");
        }
        if (!keyspace.matches(KEYSPACE_NAME_PATTERN)) {
            throw new InvalidArgumentException("Keyspace should contain alphanumerics[a-z0-9_]");
        }
        if (!regionMap.containsKey(databaseRegion)) {
            throw new InvalidArgumentException("Database region '" + databaseRegion + "' has not been found.");
        } 
        if (databaseName.length() < 3 || databaseName.length() > 50) {
            throw new InvalidArgumentException("Database name '" + databaseName + "' should have between 2 and 50 characters");
        }
        
        // if multiple databases with same name => error
        Optional<DatabaseClient> dbClient = dbDao.getDatabaseClient(databaseName);
        
        // DATABASE DOES NOT EXIST
        if (dbClient.isEmpty()) {
            LoggerShell.info("%s '%s' does not exist. Creating database '%s' with keyspace '%s'"
                    .formatted(DB, databaseName, databaseName, keyspace));
            CliContext.getInstance().getApiDevopsDatabases()
            .createDatabase(DatabaseCreationRequest.builder()
                    .name(databaseName)
                    .tier(DEFAULT_TIER)
                    .cloudProvider(CloudProviderType.valueOf(regionMap
                            .get(databaseRegion)
                            .getCloudProvider()
                            .toUpperCase()))
                    .cloudRegion(databaseRegion)
                    .keyspace(keyspace)
                    .build());
            LoggerShell.info("%s '%s' and keyspace '%s' are being created."
                    .formatted(DB, databaseName, keyspace));
        } else {

            if (!ifNotExist) {
                throw new DatabaseNameNotUniqueException(databaseName);
            }

            Database db = dbDao.getDatabase(databaseName);
            DatabaseStatusType dbStatus = db.getStatus();
            switch (dbStatus) {
                case HIBERNATED -> {
                    resumeDb(databaseName);
                    waitForDbStatus(databaseName, DatabaseStatusType.ACTIVE, DEFAULT_TIMEOUT_SECONDS);
                    dbStatus = dbDao.getDatabase(databaseName).getStatus();
                }
                case PENDING, RESUMING -> {
                    waitForDbStatus(databaseName, DatabaseStatusType.ACTIVE, DEFAULT_TIMEOUT_SECONDS);
                    dbStatus = dbDao.getDatabase(databaseName).getStatus();
                }
                default -> LoggerShell.info("%s '%s' already exist. Connecting to database.".formatted(DB, databaseName));
            }
            
            // Create keyspace on existing DB when needed
            if (DatabaseStatusType.ACTIVE.equals(dbStatus)) {
                createKeyspace(databaseName, keyspace, true);
            } else {
                throw new InvalidDatabaseStateException(databaseName, DatabaseStatusType.ACTIVE, dbStatus);
            }
        }
    }
     
    /**
     * List Databases.
     */
    public void listDb() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME,    20);
        sht.addColumn(COLUMN_ID,      37);
        sht.addColumn(COLUMN_DEFAULT_REGION, 20);
        sht.addColumn(COLUMN_STATUS,  15);
        CliContext.getInstance()
           .getApiDevopsDatabases()
           .databasesNonTerminated()
           .forEach(db -> {
                Map <String, String> rf = new HashMap<>();
                rf.put(COLUMN_NAME,    db.getInfo().getName());
                rf.put(COLUMN_ID,      db.getId());
                rf.put(COLUMN_DEFAULT_REGION, db.getInfo().getRegion());
                rf.put(COLUMN_STATUS,  db.getStatus().name());
                sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }
    
    /**
     * List keyspaces of a database.
     *
     * @param databaseName
     *      database name
     * @throws DatabaseNameNotUniqueException
     *      multiple databases with the name.
     * @throws DatabaseNotFoundException
     *      database name has not been found.
     */
    public void listKeyspaces(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Database   db  = dbDao.getDatabase(databaseName);
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME,    20);
        db.getInfo().getKeyspaces().forEach(ks -> {
            Map <String, String> rf = new HashMap<>();
            if (db.getInfo().getKeyspace().equals(ks)) {
                rf.put(COLUMN_NAME, ks + " (default)");
            } else {
                rf.put(COLUMN_NAME, ks);
            }
            sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
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
        LoggerShell.info("Deleting Database '%s' (async operation)".formatted(databaseName));
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
            case HIBERNATED -> {
                resumeDbRequest(db);
                LoggerShell.success("Database '%s' is resuming".formatted(db));
            }
            case RESUMING -> LoggerShell.info("Database '" + databaseName + "' is already resuming");
            case ACTIVE -> LoggerShell.info("Database '" + databaseName + "' is already active");
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
             if (response.statusCode() == 500) {
                 throw new RuntimeException("Cannot resume db error: %s".formatted(response.body()));
             }
        } catch (InterruptedException e) {
            LoggerShell.warning("Interrupted %s".formatted(e.getMessage()));
            Thread.currentThread().interrupt();
        } catch (Exception e) {
             LoggerShell.warning("Resuming request might have failed, please check %s"
                     .formatted(e.getMessage()));
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
    public void showDb(String databaseName, DbGetKeys key)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Database db = dbDao.getDatabase(databaseName);
        if (key == null) {
            ShellTable sht = ShellTable.propertyTable(15, 40);
            sht.addPropertyRow(COLUMN_NAME, db.getInfo().getName());
            sht.addPropertyRow(COLUMN_ID, db.getId());
            sht.addPropertyRow(COLUMN_STATUS, db.getStatus().toString());
            sht.addPropertyRow(COLUMN_DEFAULT_CLOUD, db.getInfo().getCloudProvider().name());
            sht.addPropertyRow(COLUMN_DEFAULT_REGION, db.getInfo().getRegion());
            sht.addPropertyRow(COLUMN_DEFAULT_KEYSPACE, db.getInfo().getKeyspace());
            sht.addPropertyRow("Creation Time", db.getCreationTime());
            List<String> regions   = db.getInfo().getDatacenters().stream().map(Datacenter::getRegion).collect(Collectors.toList());
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
     * Create a keyspace if not exist.
     * 
     * @param ifNotExist
     *      flag to disable error if already exists
     * @param databaseName
     *      db name
     * @param keyspaceName
     *      ks name
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     * @throws InvalidArgumentException
     *      invalid parameter
     * @throws KeyspaceAlreadyExistException
     *      keyspace exist and --if-not-exist option not provided
     */
    public void createKeyspace(String databaseName, String keyspaceName, boolean ifNotExist)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException,
           InvalidArgumentException, KeyspaceAlreadyExistException {
        
        // Validate keyspace names
        if (!keyspaceName.matches(KEYSPACE_NAME_PATTERN))
            throw new InvalidArgumentException("The keyspace name is not valid, please use snake_case: [a-z0-9_]");
        
        if (dbDao.getDatabase(databaseName).getInfo().getKeyspaces().contains(keyspaceName)) {
            if (ifNotExist) {
                LoggerShell.info("%s '%s' already exists. Connecting to keyspace.".formatted(KS, keyspaceName));
            } else {
                throw new KeyspaceAlreadyExistException(keyspaceName, databaseName);
            }
        } else {
            try {
                dbDao.getRequiredDatabaseClient(databaseName).createKeyspace(keyspaceName);
                LoggerShell.info("%s '%s' is creating.".formatted(KS, keyspaceName));
            } catch(Exception e) {
               throw new InvalidDatabaseStateException(databaseName, DatabaseStatusType.ACTIVE,
                       dbDao.getDatabase(databaseName).getStatus());
            }
        }
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
        Organization org = CliContext.getInstance().getApiDevopsOrganizations().organization();
        envFile.getKeys().put(EnvKey.ASTRA_ORG_ID, org.getId());
        envFile.getKeys().put(EnvKey.ASTRA_ORG_NAME, org.getName());
        envFile.getKeys().put(EnvKey.ASTRA_ORG_TOKEN, CliContext.getInstance().getToken());
        
        // Database
        Database db = dbDao.getDatabase(dbName);
        Map<String, Datacenter> datacenters = db
                .getInfo().getDatacenters()
                .stream().collect(Collectors.toMap(Datacenter::getRegion, Function.identity()));
        envFile.getKeys().put(EnvKey.ASTRA_DB_ID, db.getId());
        envFile.getKeys().put(EnvKey.ASTRA_DB_REGION, db.getInfo().getRegion());
        envFile.getKeys().put(EnvKey.ASTRA_DB_SECURE_BUNDLE_URL, datacenters.get(region).getSecureBundleUrl());

        if (region == null) region = db.getInfo().getRegion();
        // Parameter Validations
        Set<String> availableRegions = CliContext.getInstance()
                .getApiDevopsOrganizations()
                .regionsServerless()
                .map(DatabaseRegionServerless::getName)
                .collect(Collectors.toSet());
        if (!availableRegions.contains(region)) {
            throw new InvalidArgumentException("Provided region is invalid pick one of " + availableRegions);
        }
        Set <String> dbRegions = db.getInfo().getDatacenters()
                .stream().map(Datacenter::getRegion).collect(Collectors.toSet());
        if (!dbRegions.contains(region)) {
            throw new InvalidArgumentException("Database is not deployed in provided region");
        }
        envFile.getKeys().put(EnvKey.ASTRA_DB_REGION, region);

        // Application Token
        envFile.getKeys().put(EnvKey.ASTRA_DB_APPLICATION_TOKEN, CliContext.getInstance().getToken());
        // Cloud secure Bundle
        dbDao.downloadCloudSecureBundles(dbName);
        envFile.getKeys().put(EnvKey.ASTRA_DB_SECURE_BUNDLE_PATH, AstraCliUtils.ASTRA_HOME
                + File.separator + AstraCliUtils.SCB_FOLDER
                + AstraClientConfig.buildScbFileName(db.getId(), region));
        // GraphQL URL
        String graphQLEndpoint = ApiLocator.getApiGraphQLEndPoint(db.getId(), region);
        // Keyspace
        if (ks == null) ks = db.getInfo().getKeyspace();
        envFile.getKeys().put(EnvKey.ASTRA_DB_KEYSPACE, ks);
        envFile.getKeys().put(EnvKey.ASTRA_DB_GRAPHQL_URL, graphQLEndpoint + "/graphql/" + ks);
        envFile.getKeys().put(EnvKey.ASTRA_DB_GRAPHQL_URL_PLAYGROUND, graphQLEndpoint + "/playground");
        envFile.getKeys().put(EnvKey.ASTRA_DB_GRAPHQL_URL_SCHEMA, graphQLEndpoint + "/graphql-schema");
        envFile.getKeys().put(EnvKey.ASTRA_DB_GRAPHQL_URL_ADMIN, graphQLEndpoint + "/graphql-admin");

        // Rest URL
        String restEndpoint = ApiLocator.getApiRestEndpoint(db.getId(), region);
        envFile.getKeys().put(EnvKey.ASTRA_DB_REST_URL, restEndpoint);
        envFile.getKeys().put(EnvKey.ASTRA_DB_REST_URL_SWAGGER, restEndpoint + "/swagger-ui/");

        envFile.save();
    }

}
 