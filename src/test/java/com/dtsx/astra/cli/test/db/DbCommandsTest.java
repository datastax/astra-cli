package com.dtsx.astra.cli.test.db;

import java.io.File;
import java.util.UUID;

import com.dtsx.astra.cli.config.AstraConfiguration;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CoreOptions;
import com.dtsx.astra.cli.core.TokenOptions;
import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.core.out.OutputFormat;
import com.dtsx.astra.cli.db.ServiceDatabase;
import com.dtsx.astra.cli.db.cqlsh.ServiceCqlShell;
import com.dtsx.astra.cli.db.dsbulk.ServiceDsBulk;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class DbCommandsTest extends AbstractCmdTest {
    
    static String DB_TEST = "astra_cli_test";

    /** dataset. */
    public final static String TABLE_TEST = "test_dsbulk";

    @BeforeAll
    public static void should_create_when_needed() {
        assertSuccessCli("db create %s --if-not-exist".formatted(DB_TEST));
    }
    
    @Test
    @Order(1)
    public void testShouldShowHelp() {
        assertSuccessCli("help");
        assertSuccessCli("help db");
        assertSuccessCli("help db create");
        assertSuccessCli("help db delete");
        assertSuccessCli("help db list");
        assertSuccessCli("help db cqlsh");
        assertSuccessCli("help db resume");
        assertSuccessCli("help db status");
        assertSuccessCli("help db download-scb");
        assertSuccessCli("help db create-keyspace");
        assertSuccessCli("help db list-keyspaces");
    }
    
    @Test
    @Order(2)
    public void testShouldListDb() {
        assertSuccessCli("db list");
        assertSuccessCli("db list -v");
        assertSuccessCli("db list --no-color");
        assertSuccessCli("db list -o json");
        assertSuccessCli("db list -o csv");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "db list -w");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "db list DB");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "db list -o yaml");
    }
    
    @Test
    @Order(3)
    public void testShouldCreateDb() throws DatabaseNameNotUniqueException {
        // When
        assertSuccessCli("db create %s --if-not-exist --wait".formatted(DB_TEST));
        // Then
        Assertions.assertTrue(DaoDatabase.getInstance().getDatabaseClient(DB_TEST).isPresent());
        // Database is pending
        assertSuccessCli("db status %s".formatted(DB_TEST));
    }
    
    @Test
    @Order(4)
    public void testShouldGetDb() throws DatabaseNameNotUniqueException {
        assertSuccessCli("db get %s".formatted(DB_TEST));
        assertSuccessCli("db get %s -o json".formatted(DB_TEST));
        assertSuccessCli("db get %s -o csv".formatted(DB_TEST));
        assertSuccessCli("db get %s --key id".formatted(DB_TEST));
        assertSuccessCli("db get %s --key status".formatted(DB_TEST));
        assertSuccessCli("db get %s --key cloud".formatted(DB_TEST));
        assertSuccessCli("db get %s --key keyspace".formatted(DB_TEST));
        assertSuccessCli("db get %s --key keyspaces".formatted(DB_TEST));
        assertSuccessCli("db get %s --key region".formatted(DB_TEST));
        assertSuccessCli("db get %s --key regions".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db get %s --invalid".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db get does-not-exist");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "db get %s -o yaml".formatted(DB_TEST));
    }
    
    @Test
    @Order(5)
    public void testShouldListKeyspaces()  {
        assertSuccessCli("db list-keyspaces %s".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s -v".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s --no-color".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s -o json".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s -o csv".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db list-keyspaces does-not-exist");
    }
    
    @Test
    @Order(6)
    public void testShouldCreateKeyspaces()  {
        String randomKS = "ks_" + UUID
                .randomUUID().toString()
                .replaceAll("-", "").substring(0, 8);
        assertSuccessCli("db create-keyspace %s -k %s -v ".formatted(DB_TEST, randomKS));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db create-keyspace %s -k %s -v".formatted("does-not-exist", randomKS));
    }
    
    @Test
    @Order(7)
    public void testShouldDownloadScb()  {
        assertSuccessCli("db download-scb %s -f %s".formatted(DB_TEST, "/tmp/sample.zip"));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db download-scb %s".formatted("invalid"));
    }

    @Test
    @Order(8)
    public void testShouldCreateDotenv()  {
        if (!disableTools) {
            assertSuccessCli("db create-dotenv %s -d %s".formatted(DB_TEST, "/tmp/"));
        }
    }
    
    @Test
    @Order(9)
    public void testShouldResumeDb()  {
        assertSuccessCli("db create-keyspace %s -k %s --if-not-exist --wait".formatted(DB_TEST, "kkk2"));
        assertSuccessCli("db resume %s --wait".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db resume %s".formatted("invalid"));
    }

    @Test
    @Order(10)
    public void testShouldInstallCqlsh() {
        if (!disableTools) {
            new File(AstraCliUtils.ASTRA_HOME + File.separator + "cqlsh-astra").delete();
            ServiceCqlShell.getInstance().install();
            Assertions.assertTrue(ServiceCqlShell.getInstance().isInstalled());
        }
    }

    @Test
    @Order(11)
    public void testShouldStartShell() {
        if (!disableTools) {
            assertSuccessCli("db", "cqlsh", DB_TEST, "-e", "SELECT cql_version FROM system.local");
        }
    }

    @Test
    @Order(12)
    public void testShouldInstallDsbulk() {
        if (!disableTools) {
            // delete previous install
            new File(AstraCliUtils.ASTRA_HOME + File.separator
                    + "dsbulk-"
                    + AstraCliUtils.readProperty("dsbulk.version")).delete();
            // install
            ServiceDsBulk.getInstance().install();
            Assertions.assertTrue(ServiceDsBulk.getInstance().isInstalled());
        }
    }

    @Test
    @Order(13)
    public void testShouldCount() {
        if (!disableTools) {
            // Given
            assertSuccessCli("db", "create", DB_TEST, "--if-not-exists", "--wait");
            // Cqlsh is installed if needed
            assertSuccessCli("db", "cqlsh", DB_TEST, "-e", ""
                    + "CREATE TABLE IF NOT EXISTS "
                    + DB_TEST + "." + TABLE_TEST + "(id text PRIMARY KEY);"
                    + "INSERT INTO " + DB_TEST + "." + TABLE_TEST
                    + "(id) VALUES('a');");
            // When
            assertSuccessCli("db", "count", DB_TEST,
                    "-k", DB_TEST,
                    "-t", TABLE_TEST,
                    "-logDir", "/tmp");
        }
    }

    @Test
    @Order(14)
    public void testShouldImport() {
        if (!disableTools){
            assertSuccessCli("db", "load", DB_TEST,
                    "-k", DB_TEST,
                    "-t", TABLE_TEST,
                    "-url", "src/test/resources/test_dataset.csv",
                    "-logDir", "/tmp");
        }
    }

    @Test
    @Order(15)
    public void testShouldExport() {
        if (!disableTools) {
            assertSuccessCli("db", "unload", DB_TEST, "-k", DB_TEST,"-t", TABLE_TEST,
                    "-url", "/tmp/export-"+ DB_TEST + "-" + TABLE_TEST,
                    "-logDir", "/tmp");
        }
    }

    @Test
    @Order(16)
    public void testShouldThrowDatabaseAlreadyExist() {
        assertExitCodeCli(ExitCode.ALREADY_EXIST, "db create %s".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.ALREADY_EXIST, "db create-keyspace %s -k %s".formatted(DB_TEST, DB_TEST));
    }

    @Test
    @Order(17)
    public void testShouldThrowDatabaseInvalidState() {
        // Given
        // Create a temporary db without waiting, expecting status PENDING
        assertSuccessCli("db create %s --async".formatted("tmp_db"));
        // When, Then
        assertExitCodeCli(ExitCode.UNAVAILABLE, "db create-keyspace %s -k %s".formatted("tmp_db", "ks"));
        // Waiting for the db to be started
        assertSuccessCli("db create %s --if-not-exist".formatted("tmp_db"));
        // Delete db
        assertSuccessCli("db delete %s".formatted("tmp_db"));
    }

    @Test
    @Order(19)
    public void testShouldThrowInvalidArgument()  {
        Assertions.assertThrows(InvalidArgumentException.class, () -> {
            CliContext.getInstance().init(new CoreOptions(false,false,
                    OutputFormat.HUMAN,
                    AstraConfiguration.getDefaultConfigurationFileName()));
            CliContext.getInstance().initToken(new TokenOptions(null, AstraConfiguration.ASTRARC_DEFAULT));
            ServiceDatabase.getInstance().generateDotEnvFile(DB_TEST, DB_TEST, "invalid", "/tmp");
        });
    }

    @Test
    @Order(20)
    public void showToolsURL() {
        assertSuccessCli("db swagger %s".formatted(DB_TEST));
        assertSuccessCli("db playground %s".formatted(DB_TEST));
    }

    //@AfterAll
    //public static void testShouldDelete() {
    //    assertSuccessCli("db delete %s".formatted(DB_TEST));
    //}

}
