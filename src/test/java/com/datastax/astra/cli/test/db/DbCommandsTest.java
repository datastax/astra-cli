package com.datastax.astra.cli.test.db;

import java.io.File;
import java.util.UUID;

import com.datastax.astra.cli.db.cqlsh.CqlShellService;
import com.datastax.astra.cli.db.dsbulk.DsBulkService;
import com.datastax.astra.cli.utils.AstraCliUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.db.DatabaseDao;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.test.AbstractCmdTest;

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
        assertSuccessCli("db create %s --if-not-exist --wait".formatted(DB_TEST));
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
        assertSuccessCli("help db dsbulk");
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
        Assertions.assertTrue(DatabaseDao.getInstance().getDatabaseClient(DB_TEST).isPresent());
        // Database is pending
        assertSuccessCli("db status %s".formatted(DB_TEST));
    }
    
    @Test
    @Order(4)
    public void testShoulGetDb() throws DatabaseNameNotUniqueException {
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
        assertSuccessCli("db create-keyspace %s -k %s -v".formatted(DB_TEST, randomKS));
        assertExitCodeCli(ExitCode.NOT_FOUND, 
                "db create-keyspace %s -k %s -v".formatted("does-not-exist", randomKS));
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
        assertSuccessCli("db create-dotenv %s -d %s".formatted(DB_TEST, "/tmp/"));
        Assertions.assertTrue(new File("/tmp/.env").exists());
    }
    
    @Test
    @Order(9)
    public void testShouldResumeDb()  {
        assertSuccessCli("db resume %s".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db resume %s".formatted("invalid"));
    }

    @Test
    @Order(10)
    public void testShouldInstallCqlsh() {
        if (!disableTools) {
            new File(AstraCliUtils.ASTRA_HOME + File.separator + "cqlsh-astra").delete();
            CqlShellService.getInstance().install();
            Assertions.assertTrue(CqlShellService.getInstance().isInstalled());
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
            DsBulkService.getInstance().install();
            Assertions.assertTrue(DsBulkService.getInstance().isInstalled());
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
            // When
            assertSuccessCli("db", "dsbulk", DB_TEST,  "count",
                    "-k", DB_TEST,
                    "-t", TABLE_TEST,
                    "-logDir", "/tmp");

        }
    }

    @Test
    @Order(14)
    public void testShouldDeleteDb()  {
        assertSuccessCli("db delete %s".formatted(DB_TEST));
    }
}
