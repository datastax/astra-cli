package com.datastax.astra.cli.db;

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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
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
import com.datastax.astra.cli.db.exception.KeyspaceNotFoundException;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.datastax.astra.cli.utils.EnvFile;
import com.datastax.astra.cli.utils.EnvFile.EnvKey;
import com.datastax.astra.sdk.databases.DatabaseClient;
import com.datastax.astra.sdk.databases.domain.CloudProviderType;
import com.datastax.astra.sdk.databases.domain.Database;
import com.datastax.astra.sdk.databases.domain.DatabaseCreationRequest;
import com.datastax.astra.sdk.databases.domain.DatabaseRegionServerless;
import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;
import com.datastax.astra.sdk.databases.domain.Datacenter;
import com.datastax.astra.sdk.organizations.domain.Organization;
import com.datastax.astra.sdk.utils.ApiLocator;

/**
 * Service layer to work with database.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabaseService implements DatabaseConstants {
    
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
               long start = System.currentTimeMillis();
               if (db.getStatus().equals(status)) {
                   return ExitCode.SUCCESS;
               }
               LoggerShell.success("Database '%s' has status '%s' waiting to be '%s' ..."
                       .formatted(databaseName, db.getStatus(), status));
               
               int retries = 0;
               while (retries++ < timeout && !db.getStatus().equals(status)) {
                   try {
                    Thread.sleep(1000);
                    db = dbDao.getDatabase(databaseName);
                    LoggerShell.debug("Waiting for database to become " + status + 
                            " but was " + db.getStatus() + " retrying (" + retries + "/" + timeout + ")");
                   } catch (InterruptedException e) {
                       LoggerShell.error("Interupted operation: %s".formatted(e.getMessage()));
                       Thread.currentThread().interrupt();
                   }
               }
               // Success if you did not reach the timeout (meaning status is good)
               if (retries < timeout) {
                   LoggerShell.success("Database '"
                           + databaseName + "' has status '"  +  status 
                           + "' (took " + (System.currentTimeMillis() - start) + " millis)");
                   return ExitCode.SUCCESS;
               }
               LoggerShell.warning("Timeout (" + timeout + "s) : "
                       + "Database '" + databaseName + "' status is not yet '" + status 
                       + "' (current status '" + db.getStatus() + "')");
               return ExitCode.UNAVAILABLE;
            }
        }
        LoggerShell.error("Database '" + databaseName + "' has not been found.");
        return ExitCode.NOT_FOUND;
    }
    
    /**
     * Mutualization of create db code (shell and cli)
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
     *      error when db not found (when createing keyspace)
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
                    .replaceAll(" ", "_")
                    .replaceAll("-", "_");
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
        if (!dbClient.isPresent()) {
            LoggerShell.success("Database '" + databaseName
                    + "' does not exist. Creating database '" + databaseName 
                    + "' with keyspace '" + keyspace + "'");
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
            AstraCliConsole.outputSuccess("Database '" + databaseName + "' and keyspace '" + keyspace + "'are being created.");
        }
        
        // A single instance of the DB exist
        LoggerShell.success("Database '" + databaseName + "' already exist. Connecting to database.");
        
        if (!ifNotExist) {
            LoggerShell.warning("Cannot create another database with name '" + databaseName + ". Use flag --if-not-exist to connect to the existing database");
            throw new DatabaseNameNotUniqueException(databaseName);
        }
        
        // If the database is HIBERNATED we need to wake it up before assessing the keyspace
        if (dbClient.isPresent()) {
            Database db = dbDao.getDatabase(databaseName);
            DatabaseStatusType dbStatus = db.getStatus();
            switch (dbStatus) {
                case HIBERNATED:
                    resumeDb(databaseName);
                    waitForDbStatus(databaseName, DatabaseStatusType.ACTIVE, 180);
                    dbStatus = db.getStatus();
                break;
                case PENDING:
                case RESUMING:
                    waitForDbStatus(databaseName, DatabaseStatusType.ACTIVE, 180);
                    dbStatus = db.getStatus();
                break;
                default:
                break;
            }
            
            // Create keyspace on existing DB when needed
            if (dbStatus.equals(DatabaseStatusType.ACTIVE)) {
                createKeyspace(databaseName, keyspace, ifNotExist);
            } else {
                LoggerShell.error("Database '" + databaseName + "' already exists "
                        + "but was neither ACTIVE not HIBERNATED but '" + dbStatus + "', cannot create keyspace.");
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
     * Delete a dabatase if exist.
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
        AstraCliConsole.outputSuccess("Deleting Database '%s' (async operation)".formatted(databaseName));
    }
  
    /**
     * Resume a dabatase if exist.
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
        switch(db.getStatus()) {
            case HIBERNATED:
                resumeDbRequest(db);
                break;
            case RESUMING:
                LoggerShell.warning("Database '" + databaseName + "'is already resuming");
                break;
            default:
                LoggerShell.warning("Your database has not 'HIBERNATED' status.");
                throw new InvalidDatabaseStateException(databaseName, DatabaseStatusType.HIBERNATED, db.getStatus());
        }
    }
    
    /**
     * Database name.
     *
     * @param db
     *      database name
     * @return
     *      evualurate success of the operation
     */
    private int resumeDbRequest(Database db) {
        try {
            // Compute Endpoint for the Keyspaces
            String endpoint = new StringBuilder(ApiLocator.getApiRestEndpoint(db.getId(), db.getInfo().getRegion()))
                .append("/v2/schemas/keyspace")
                .toString();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("X-Cassandra-Token", CliContext.getInstance().getToken())
                    .GET()
                    .build();
            
             HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
             return response.statusCode();
        } catch (Exception e) {
             LoggerShell.warning("Resuming request might have failed, please check %s"
                     .formatted(e.getMessage()));
             return 500;
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
            switch(CliContext.getInstance().getOutputFormat()) {
                case csv:
                    sht.addPropertyRow(COLUMN_REGIONS, regions.toString());
                    sht.addPropertyRow(COLUMN_KEYSPACES, keyspaces.toString());
                    AstraCliConsole.printShellTable(sht);
                break;
                case json:
                    AstraCliConsole.printJson(new JsonOutput(ExitCode.SUCCESS, 
                                DB + " " + AbstractConnectedCmd.GET + " " + databaseName, db));
                break;
                case human:
                default:
                    sht.addPropertyListRows(COLUMN_KEYSPACES, keyspaces);
                    sht.addPropertyListRows(COLUMN_REGIONS, regions);
                    AstraCliConsole.printShellTable(sht);
                break;
             }
         } else {
            switch(key) {
                case id:
                    AstraCliConsole.println(db.getId());
                break;
                case cloud:
                    AstraCliConsole.println(db.getInfo().getCloudProvider().name());
                break;
                case keyspace:
                    AstraCliConsole.println(db.getInfo().getKeyspace());
                break;
                case keyspaces:
                    AstraCliConsole.println(new ArrayList<>(db.getInfo().getKeyspaces()).toString());
                break;
                case region:
                    AstraCliConsole.println(db.getInfo().getRegion());
                break;
                case regions:
                    AstraCliConsole.println(db.getInfo().getDatacenters()
                                         .stream()
                                         .map(Datacenter::getRegion)
                                         .collect(Collectors.toList())
                                         .toString());
                break;
                case status:
                    AstraCliConsole.println(db.getStatus().toString());
                break;
            }
            
         }
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
        dbDao.getRequiredDatabaseClient(databaseName)
            .downloadAllSecureConnectBundles(
                    AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER);
        LoggerShell.info("Secure connect bundles have been downloaded.");
    }
    
    /**
     * Delete a keyspace if exist
     * 
     * @param databaseName
     *      db name
     * @param keyspaceName
     *      ks name
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     * @throws KeyspaceNotFoundException
     *      keyspace has not been found.
     */
    public void deleteKeyspace(String databaseName, String keyspaceName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, KeyspaceNotFoundException {
        Set<String> existingkeyspaces = dbDao.getDatabase(databaseName).getInfo().getKeyspaces();
        if (!existingkeyspaces.contains(keyspaceName)) {
            throw new KeyspaceNotFoundException(keyspaceName);
        }
        AstraCliConsole.outputWarning(ExitCode.NOT_IMPLEMENTED, "Astra does not provide endpoint to delete a keyspace");
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
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException, KeyspaceAlreadyExistException { 
        
        // Validate keyspace names
        if (!keyspaceName.matches(KEYSPACE_NAME_PATTERN)) {
            throw new InvalidArgumentException("The keyspace name is not valid, please use snake_case: [a-z0-9_]");
        }
        
        Set<String> existingkeyspaces = dbDao.getDatabase(databaseName).getInfo().getKeyspaces();
        if (existingkeyspaces.contains(keyspaceName)) {
            if (ifNotExist) {
                LoggerShell.success("Keyspace '" + keyspaceName + "' already exists. Connecting to keyspace.");
            } else {
                LoggerShell.error("Keyspace '" + keyspaceName + "' already exists. Cannot create "
                        + "another keyspace with same name. Use flag --if-not-exist to connect to the existing keyspace.");
                throw new KeyspaceAlreadyExistException(keyspaceName, databaseName);
            }
        } else {
            dbDao.getRequiredDatabaseClient(databaseName).createKeyspace(keyspaceName);
            LoggerShell.info("Keyspace '"+ keyspaceName + "' is creating.");
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
        envFile.getKeys().put(EnvKey.ASTRA_DB_ID, db.getId());
        envFile.getKeys().put(EnvKey.ASTRA_DB_REGION, db.getInfo().getRegion());
        if (region != null) {
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
        }
        
        // keep adding keys
        
        
        envFile.save();
    }

}
 