package com.datastax.astra.cli.db;

import static com.datastax.astra.cli.db.cqlsh.CqlShellUtils.installCqlShellAstra;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.core.out.JsonOutput;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.out.ShellPrinter;
import com.datastax.astra.cli.core.out.ShellTable;
import com.datastax.astra.cli.db.DbGetCmd.DbGetKeys;
import com.datastax.astra.cli.db.cqlsh.CqlShellOption;
import com.datastax.astra.cli.db.cqlsh.CqlShellUtils;
import com.datastax.astra.cli.db.dsbulk.DsBulkUtils;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.InvalidDatabaseStateException;
import com.datastax.astra.cli.db.exception.KeyspaceAlreadyExistException;
import com.datastax.astra.cli.db.exception.KeyspaceNotFoundException;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.datastax.astra.cli.utils.EnvFile;
import com.datastax.astra.cli.utils.EnvFile.EnvKey;
import com.datastax.astra.cli.utils.FileUtils;
import com.datastax.astra.sdk.databases.DatabaseClient;
import com.datastax.astra.sdk.databases.DatabasesClient;
import com.datastax.astra.sdk.databases.domain.CloudProviderType;
import com.datastax.astra.sdk.databases.domain.Database;
import com.datastax.astra.sdk.databases.domain.DatabaseCreationRequest;
import com.datastax.astra.sdk.databases.domain.DatabaseRegionServerless;
import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;
import com.datastax.astra.sdk.databases.domain.Datacenter;
import com.datastax.astra.sdk.organizations.domain.Organization;
import com.datastax.astra.sdk.utils.ApiLocator;

/**
 * Utility class for command `db`
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class OperationsDb {

    /** Command constants. */
    public static final String DB                    = "db";
    
    /** Command constants. */
    public static final String CMD_KEYSPACE          = "keyspace";
    
    /** Command constants. */
    public static final String CMD_INFO              = "info";
    
    /** Command constants. */
    public static final String CMD_STATUS            = "status";
    
    /** Command constants. */
    public static final String CMD_CREATE_KEYSPACE   = "create-keyspace";
    
    /** Command constants. */
    public static final String CMD_DELETE_KEYSPACE   = "delete-keyspace";
    
    /** Command constants. */
    public static final String CMD_LIST_KEYSPACES   = "list-keyspaces";
    
    /** Command constants. */
    public static final String CMD_CREATE_REGION     = "create-region";
    
    /** Command constants. */
    public static final String CMD_DELETE_REGION     = "delete-region";
    
    /** Command constants. */
    public static final String CMD_DOWNLOAD_SCB      = "download-scb";
    
    /** Command constants. */
    public static final String CMD_RESUME            = "resume";
    
    /** Default region. **/
    public static final String DEFAULT_REGION        = "us-east-1";
    
    /** Default tier. **/
    public static final String DEFAULT_TIER          = "serverless";
    
    /** Allow Snake case. */
    public static final String KEYSPACE_NAME_PATTERN = "^[_a-z0-9]+$";
    
    /** column names. */
    public static final String COLUMN_ID                = "id";
    /** column names. */
    public static final String COLUMN_NAME              = "Name";
    /** column names. */
    public static final String COLUMN_DEFAULT_REGION    = "Default Region";
    /** column names. */
    public static final String COLUMN_REGIONS           = "Regions";
    /** column names. */
    public static final String COLUMN_DEFAULT_CLOUD     = "Default Cloud Provider";
    /** column names. */
    public static final String COLUMN_STATUS            = "Status";
    /** column names. */
    public static final String COLUMN_DEFAULT_KEYSPACE  = "Default Keyspace";
    /** column names. */
    public static final String COLUMN_KEYSPACES         = "Keyspaces";
    /** column names. */
    public static final String COLUMN_CREATION_TIME     = "Creation Time";
    
    /**
     * Hide default constructor.
     */
    private OperationsDb() {}
    
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
    public static Optional<DatabaseClient> getDatabaseClient(String db) 
    throws DatabaseNameNotUniqueException {
        DatabasesClient dbsClient = ShellContext.getInstance().getApiDevopsDatabases();
        
        // Escape special chars
        db = db.replaceAll("\"", "");
        // Database name containing spaces cannot be an id
        if (!db.contains(" ") ) {
            DatabaseClient dbClient = dbsClient.database(db);
            if (dbClient.exist()) {
                LoggerShell.debug("Database found id=" + dbClient.getDatabaseId());
                return Optional.ofNullable(dbClient);
            }
        }
        
        // Not found, try with the name
        List<Database> dbs = dbsClient
                .databasesNonTerminatedByName(db)
                .collect(Collectors.toList());
        
        // Multiple databases with the same name
        if (dbs.size() > 1) {
            throw new DatabaseNameNotUniqueException(db);
        }
        
        // Database exists and is unique
        if (1 == dbs.size()) {
            LoggerShell.debug("Database found id=" + dbs.get(0).getId());
            return Optional.ofNullable(dbsClient.database(dbs.get(0).getId()));
        }
        
        LoggerShell.warning("Database " + db + " has not been found");
        return Optional.empty();
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
     */
    public static ExitCode waitForDbStatus(String databaseName, DatabaseStatusType status, int timeout) 
    throws DatabaseNameNotUniqueException {
        Optional<DatabaseClient> dbClient = getDatabaseClient(databaseName);
        if (dbClient.isPresent()) {
           Database db = dbClient.get().find().get();
           int retries = 0;
           long start = System.currentTimeMillis();
           if (db.getStatus().equals(status)) {
               return ExitCode.SUCCESS;
           }
           LoggerShell.success("Database '" + databaseName + "' has status '" + db.getStatus() + "' waiting to be '" + status + "' ...");
           while (retries++ < timeout && !db.getStatus().equals(status)) {
               try {
                Thread.sleep(1000);
                db = dbClient.get().find().get();
                LoggerShell.debug("Waiting for database to become " + status + 
                        " but was " + db.getStatus() + " retrying (" + retries + "/" + timeout + ")");
               } catch (InterruptedException e) {}
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
     * @throws DatabaseNotAvailableException
     *      database is hibernating or error state, cannot proceed 
     */
    public static void createDb(String databaseName, String databaseRegion, String keyspace, boolean ifNotExist) 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, 
           InvalidDatabaseStateException, InvalidArgumentException, KeyspaceAlreadyExistException {
        
        // Parameter Validations
        Map<String, DatabaseRegionServerless> regionMap = ShellContext.getInstance()
                .getApiDevopsOrganizations()
                .regionsServerless()
                .collect(Collectors
                .toMap(DatabaseRegionServerless::getName, Function.identity()));
        if (StringUtils.isEmpty(keyspace)) {
            keyspace = databaseName.toLowerCase()
                    .replaceAll(" ", "_")
                    .replaceAll("-", "_");
        }
        if (!keyspace.matches(OperationsDb.KEYSPACE_NAME_PATTERN)) {
            throw new InvalidArgumentException("Keyspace should contain alphanumerics[a-z0-9_]");
        }
        if (!regionMap.containsKey(databaseRegion)) {
            throw new InvalidArgumentException("Database region '" + databaseRegion + "' has not been found.");
        } 
        if (databaseName.length() < 3 || databaseName.length() > 50) {
            throw new InvalidArgumentException("Database name '" + databaseName + "' should have between 2 and 50 characters");
        }
        
        // if multiple databases with same name => error
        Optional<DatabaseClient> dbClient = getDatabaseClient(databaseName);
        
        // DATABASE DOES NOT EXIST
        if (!dbClient.isPresent()) {
            LoggerShell.success("Database '" + databaseName
                    + "' does not exist. Creating database '" + databaseName 
                    + "' with keyspace '" + keyspace + "'");
            ShellContext.getInstance().getApiDevopsDatabases()
            .createDatabase(DatabaseCreationRequest.builder()
                    .name(databaseName)
                    .tier(OperationsDb.DEFAULT_TIER)
                    .cloudProvider(CloudProviderType.valueOf(regionMap
                            .get(databaseRegion)
                            .getCloudProvider()
                            .toUpperCase()))
                    .cloudRegion(databaseRegion)
                    .keyspace(keyspace)
                    .build());
            ShellPrinter.outputSuccess("Database '" + databaseName + "' and keyspace '" + keyspace + "'are being created.");
        }
        
        // A single instance of the DB exist
        LoggerShell.success("Database '" + databaseName + "' already exist. Connecting to database.");
        
        if (!ifNotExist) {
            LoggerShell.warning("Cannot create another database with name '" + databaseName + ". Use flag --if-not-exist to connect to the existing database");
            throw new DatabaseNameNotUniqueException(databaseName);
        }
        
        // If the database is HIBERNATED we need to wake it up before assessing the keyspace
        Database db = dbClient.get().find().get();
        DatabaseStatusType dbStatus = db.getStatus();
        if (dbStatus.equals(DatabaseStatusType.HIBERNATED)) {
            OperationsDb.resumeDb(databaseName);
            OperationsDb.waitForDbStatus(databaseName, DatabaseStatusType.ACTIVE, 180);
            db = dbClient.get().find().get();
            dbStatus = db.getStatus();
        }
        
        // Create keyspace on existing DB when needed
        if (dbStatus.equals(DatabaseStatusType.ACTIVE)) {
            OperationsDb.createKeyspace(databaseName, keyspace, ifNotExist);
        } else {
            LoggerShell.error("Database '" + databaseName + "' already exists "
                    + "but was neither ACTIVE not HIBERNATED but '" + dbStatus + "', cannot create keyspace.");
            throw new InvalidDatabaseStateException(databaseName, DatabaseStatusType.ACTIVE, dbStatus);
        }
    }
     
    /**
     * List Databases.
     */
    public static void listDb() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME,    20);
        sht.addColumn(COLUMN_ID,      37);
        sht.addColumn(COLUMN_DEFAULT_REGION, 20);
        sht.addColumn(COLUMN_STATUS,  15);
        ShellContext.getInstance()
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
        ShellPrinter.printShellTable(sht);
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
    public static void listKeyspaces(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Optional<DatabaseClient> dbClient = getDatabaseClient(databaseName);
        if (!dbClient.isPresent()) {
            throw new DatabaseNotFoundException(databaseName);
        }
        Database db = dbClient.get().find().get();
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
        ShellPrinter.printShellTable(sht);
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
    public static void deleteDb(String databaseName) 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Optional<DatabaseClient> dbClient = getDatabaseClient(databaseName);
        if (!dbClient.isPresent()) {
            throw new DatabaseNotFoundException(databaseName);
        }
        dbClient.get().delete();
        ShellPrinter.outputSuccess("Deleting Database '" + databaseName + "' (async operation)");
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
    public static void resumeDb(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, 
           InvalidDatabaseStateException {
        Optional<DatabaseClient> dbClient = getDatabaseClient(databaseName);
        if (!dbClient.isPresent()) {
            throw new DatabaseNotFoundException(databaseName);
        }
        Database db = dbClient.get().find().get();
        switch(db.getStatus()) {
            case HIBERNATED:
                resumeDbRequest(db);
            case RESUMING:
                LoggerShell.warning("Database '" + databaseName + "'is already resuming");
            break;
            default:
                LoggerShell.warning("Your database has not 'HIBERNATED' status.");
                throw new InvalidDatabaseStateException(databaseName, 
                        DatabaseStatusType.HIBERNATED, db.getStatus());
        }
    }
    
    /**
     * Database name.
     *
     * @param db
     *      database name
     */
    private static void resumeDbRequest(Database db) {
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Forge request (minimal dependencies)
            StringBuilder keyspacesUrl = new StringBuilder(
                        ApiLocator.getApiRestEndpoint(
                                db.getId(), 
                                db.getInfo().getRegion()));
            keyspacesUrl.append("/v2/schemas/keyspace");
            HttpUriRequestBase req = new HttpGet(keyspacesUrl.toString());
            req.addHeader("accept", "application/json");
            req.addHeader("X-Cassandra-Token", ShellContext.getInstance().getToken());
            httpClient.execute(req);
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
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
    public static void showDbStatus(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Optional<DatabaseClient> dbClient = getDatabaseClient(databaseName);
        if (!dbClient.isPresent()) {
            throw new DatabaseNotFoundException(databaseName);
        }
        Database db = dbClient.get().find().get();
        ShellPrinter.outputSuccess("Database '" + databaseName + "' has status '" + db.getStatus() + "'");
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
     *      error when db does not exists
     */
    private static Database getDatabase(String databaseName) 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Optional<DatabaseClient> dbClient = getDatabaseClient(databaseName);
        if (!dbClient.isPresent()) {
            throw new DatabaseNotFoundException(databaseName);
        }
        return dbClient.get().find().get();
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
     * @return
     *      status code
     */
    public static void showDb(String databaseName, DbGetKeys key)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        Database db = getDatabase(databaseName);
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
            switch(ShellContext.getInstance().getOutputFormat()) {
                case csv:
                    sht.addPropertyRow(COLUMN_REGIONS, regions.toString());
                    sht.addPropertyRow(COLUMN_KEYSPACES, keyspaces.toString());
                    ShellPrinter.printShellTable(sht);
                break;
                case json:
                    ShellPrinter.printJson(new JsonOutput(ExitCode.SUCCESS, 
                                OperationsDb.DB + " " + AbstractConnectedCmd.GET + " " + databaseName, db));
                break;
                case human:
                default:
                    sht.addPropertyListRows(COLUMN_KEYSPACES, keyspaces);
                    sht.addPropertyListRows(COLUMN_REGIONS, regions);
                    ShellPrinter.printShellTable(sht);
                break;
             }
         } else {
            switch(key) {
                case id:
                    System.out.println(db.getId());
                break;
                case cloud:
                    System.out.println(db.getInfo().getCloudProvider().name());
                break;
                case keyspace:
                    System.out.println(db.getInfo().getKeyspace());
                break;
                case keyspaces:
                    System.out.println(new ArrayList<>(db.getInfo().getKeyspaces()));
                break;
                case region:
                    System.out.println(db.getInfo().getRegion());
                break;
                case regions:
                    System.out.println(db.getInfo().getDatacenters()
                                         .stream()
                                         .map(Datacenter::getRegion)
                                         .collect(Collectors.toList()));
                break;
                case status:
                    System.out.println(db.getStatus().toString());
                break;
            }
            
         }
    }
    
    /**
     * Download SCB when needed.
     *
     * @param databaseName
     *      database name.
     * @param dir
     *      directory to save the zip
     * @param file
     *      filenames
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     * @throws InvalidArgumentException
     *      invalid argument to download sb. 
     */
    public static void downloadCloudSecureBundles(String databaseName, String dir, String file)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException {
        // Default path will be current location
        if (dir == null && file == null) { 
            dir = ".";
        }
        
        Database db = getDatabase(databaseName);
        Set<Datacenter> dcs = db.getInfo().getDatacenters();
        if (dir != null) {
            File targetFolder = new File (dir);
            if (!targetFolder.exists() || !targetFolder.isDirectory() || !targetFolder.canWrite()) {
                LoggerShell.error("You provided an invalid folder, check the -d parameters");
                throw new InvalidArgumentException("Destination folder cannot be found or written.");
            }
            getDatabaseClient(databaseName).get().downloadAllSecureConnectBundles(dir);
            LoggerShell.info("Secure connect bundles have been downloaded.");
            
        } else if (file != null) {
            
            if (dcs.size() > 1) {
                LoggerShell.error("You provided a filename but your database has multiple regions, use option -d instead");
                throw new InvalidArgumentException("\"You provided a filename but your database has multiple regions, "
                        + "use option -d instead\"");
            }
            
            // Download 1 file
            FileUtils.downloadFile(dcs.iterator().next().getSecureBundleUrl(), file);
            LoggerShell.info("Secure connect bundles have been downloaded.");
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
    public static void downloadCloudSecureBundles(String databaseName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {
        getDatabase(databaseName);
        getDatabaseClient(databaseName)
            .get()
            .downloadAllSecureConnectBundles(AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER);
        LoggerShell.info("Secure connect bundles have been downloaded.");
    }
    
    /**
     * Delete a keyspace if exist
     * 
     * @param databaseName
     *      db name
     * @param keyspaceName
     *      ks name
     * @return
     *      exit code
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     * @throws KeyspaceNotFoundException
     *      keyspace has not been found.
     */
    public static void deleteKeyspace(String databaseName, String keyspaceName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, KeyspaceNotFoundException {
        // Validate db Name
        Optional<DatabaseClient> dbClient = getDatabaseClient(databaseName);
        if (!dbClient.isPresent()) {
            throw new DatabaseNotFoundException(databaseName);
        }
        Set<String> existingkeyspaces = dbClient.get().find().get().getInfo().getKeyspaces();
        if (!existingkeyspaces.contains(keyspaceName)) {
            throw new KeyspaceNotFoundException(keyspaceName);
        }
        ShellPrinter.outputWarning(ExitCode.NOT_IMPLEMENTED, "Astra does not provide endpoint to delete a keyspace");
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
    public static void createKeyspace(String databaseName, String keyspaceName, boolean ifNotExist)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException, KeyspaceAlreadyExistException { 
        
        // Validate keyspace names
        if (!keyspaceName.matches(OperationsDb.KEYSPACE_NAME_PATTERN)) {
            throw new InvalidArgumentException("The keyspace name is not valid, please use snake_case: [a-z0-9_]");
        }
        
        Set<String> existingkeyspaces = getDatabase(databaseName).getInfo().getKeyspaces();
        if (existingkeyspaces.contains(keyspaceName)) {
            if (ifNotExist) {
                LoggerShell.success("Keyspace '" + keyspaceName + "' already exists. Connecting to keyspace.");
            } else {
                LoggerShell.error("Keyspace '" + keyspaceName + "' already exists. Cannot create "
                        + "another keyspace with same name. Use flag --if-not-exist to connect to the existing keyspace.");
                throw new KeyspaceAlreadyExistException(keyspaceName, databaseName);
            }
        } else {
            getDatabaseClient(databaseName).get().createKeyspace(keyspaceName);
            LoggerShell.info("Keyspace '"+ keyspaceName + "' is creating.");
        }
    }
    
    /**
     * Start CqlShell when needed.
     * 
     * @param options
     *      shell options
     * @param database
     *      current db
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     * @throws CannotStartProcessException
     *      cannot start third party process
     * @throws FileSystemException
     *      cannot access file system 
     */
    public static void startCqlShell(CqlShellOption options, String database)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, 
           CannotStartProcessException, FileSystemException {
        
        // Install Cqlsh for Astra and set permissions
        installCqlShellAstra();
        
        // Download scb and throw DatabaseNotFound.
        downloadCloudSecureBundles(database);    
        
        try {
            System.out.println("\nCqlsh is starting please wait for connection establishment...");
            Process cqlShProc = CqlShellUtils.runCqlShellAstra(options, getDatabase(database));
            if (cqlShProc == null) {
                throw new CannotStartProcessException("cqlsh");
            }
            cqlShProc.waitFor();
        } catch (Exception e) {
            throw new CannotStartProcessException("cqlsh", e);
        }
    }
    
    /**
     * Start DsBulk when needed.
     * 
     * @param options
     *      dsbulks options, database name is the first argument
     * @throws DatabaseNameNotUniqueException 
     *      error if db name is not unique
     * @throws DatabaseNotFoundException 
     *      error is db is not found
     * @throws CannotStartProcessException
     *      cannot start the ds bulk third party process 
     * @throws FileSystemException
     *      cannot untar on file system
     */
    public static void runDsBulk(List<String> options)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, 
           CannotStartProcessException, FileSystemException {
        String database = options.get(0);
        
        // Install dsbulk for Astra and set permissions
        DsBulkUtils.installDsBulk();
        
        // Download scb and throw DatabaseNotFound.
        downloadCloudSecureBundles(database);  
        
        try {
            System.out.println("\nDSBulk is starting please wait ...");
            Process dsbulkProc = DsBulkUtils.runDsBulk(
                    getDatabase(database), 
                    options.subList(1, options.size()));
            if (dsbulkProc == null) {
                throw new CannotStartProcessException("dsbulk");
            }
            dsbulkProc.waitFor();
           
        } catch (Exception e) {
            LoggerShell.error("Cannot start DsBulk:" + e.getMessage());
            throw new CannotStartProcessException("dsbulk", e);
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
     * @return
     *      error code
     * @throws DatabaseNotFoundException 
     *      error db not found
     * @throws DatabaseNameNotUniqueException
     *      error db not unique
     * @throws InvalidArgumentException
     *      invalid argument
     */
    public static void generateDotEnvFile(String dbName, String ks, String region, String dest) 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException {
        EnvFile envFile = new EnvFile(dest);
        // Organization Block
        Organization org = ShellContext.getInstance().getApiDevopsOrganizations().organization();
        envFile.getKeys().put(EnvKey.ASTRA_ORG_ID, org.getId());
        envFile.getKeys().put(EnvKey.ASTRA_ORG_NAME, org.getName());
        envFile.getKeys().put(EnvKey.ASTRA_ORG_TOKEN, ShellContext.getInstance().getToken());
        
        // Database
        Database db = getDatabase(dbName);
        envFile.getKeys().put(EnvKey.ASTRA_DB_ID, db.getId());
        envFile.getKeys().put(EnvKey.ASTRA_DB_REGION, db.getInfo().getRegion());
        if (region != null) {
            // Parameter Validations
            Set<String> availableRegions = ShellContext.getInstance()
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
