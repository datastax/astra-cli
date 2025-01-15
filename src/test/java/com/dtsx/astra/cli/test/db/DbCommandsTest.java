package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.config.AstraCliConfiguration;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CoreOptions;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.TokenOptions;
import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.core.out.OutputFormat;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.db.ServiceDatabase;
import com.dtsx.astra.cli.db.cqlsh.ServiceCqlShell;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.util.UUID;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
class DbCommandsTest extends AbstractCmdTest {

    @BeforeAll
    static void should_create_when_needed() {
        assertSuccessCli("db create %s --if-not-exist".formatted(DB_TEST));
        assertSuccessCli("db create %s --if-not-exist --vector".formatted(DB_TEST_VECTOR));
    }


    @Test
    @Order(1)
    void testShouldShowHelp() {
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
    void testShouldListDb() {
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
    void testShouldCreateDb() throws DatabaseNameNotUniqueException {
        // When
        assertSuccessCli("db create %s --if-not-exist".formatted(DB_TEST));
        // Then
        Assertions.assertTrue(DaoDatabase.getInstance().getDatabaseClient(DB_TEST).isPresent());
        // Database is pending
        assertSuccessCli("db status %s".formatted(DB_TEST));
    }
    
    @Test
    @Order(4)
    void testShouldGetDb() throws DatabaseNameNotUniqueException {
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
    void testShouldDescribeDb() throws DatabaseNameNotUniqueException {
        assertSuccessCli("db describe %s".formatted(DB_TEST));
        assertSuccessCli("db describe %s -o json".formatted(DB_TEST));
        assertSuccessCli("db describe %s -o csv".formatted(DB_TEST));
        assertSuccessCli("db describe %s --key id".formatted(DB_TEST));
        assertSuccessCli("db describe %s --key status".formatted(DB_TEST));
        assertSuccessCli("db describe %s --key cloud".formatted(DB_TEST));
        assertSuccessCli("db describe %s --key keyspace".formatted(DB_TEST));
        assertSuccessCli("db describe %s --key keyspaces".formatted(DB_TEST));
        assertSuccessCli("db describe %s --key region".formatted(DB_TEST));
        assertSuccessCli("db describe %s --key regions".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db describe %s --invalid".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db describe does-not-exist");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "db describe %s -o yaml".formatted(DB_TEST));
    }
    
    @Test
    @Order(6)
    void testShouldListKeyspaces()  {
        assertSuccessCli("db list-keyspaces %s".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s -v".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s --no-color".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s -o json".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s -o csv".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db list-keyspaces does-not-exist");
    }
    
    @Test
    @Order(7)
    void testShouldCreateAndDropKeyspaces()  {
        // Given
        String randomKS = "ks_" + UUID
                .randomUUID().toString()
                .replaceAll("-", "").substring(0, 8);
        // When,Then
        assertSuccessCli("db create-keyspace %s -k %s -v ".formatted(DB_TEST, randomKS));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db create-keyspace %s -k %s -v".formatted("does-not-exist", randomKS));
        // When,Then
        assertSuccessCli("db delete-keyspace %s -k %s -v ".formatted(DB_TEST, randomKS));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db delete-keyspace %s -k %s -v ".formatted(DB_TEST, randomKS));
    }
    
    @Test
    @Order(8)
    void testShouldDownloadScb()  {
        assertSuccessCli("db download-scb %s -f %s".formatted(DB_TEST, "/tmp/sample.zip"));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db download-scb %s".formatted("invalid"));
    }

    @Test
    @Order(9)
    void testShouldCreateDotenv()  {
        if (!disableTools) {
            assertSuccessCli("db create-dotenv %s -d %s".formatted(DB_TEST, "/tmp/"));
        }
    }
    
    @Test
    @Order(10)
    void testShouldResumeDb()  {
        assertSuccessCli("db create-keyspace %s -k %s --if-not-exist --wait".formatted(DB_TEST, "kkk2"));
        assertSuccessCli("db resume %s --wait".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db resume %s".formatted("invalid"));
    }

    @Test
    @Order(11)
    void testShouldInstallCqlsh() {
        if (!disableTools) {
            new File(AstraCliUtils.ASTRA_HOME + File.separator + "cqlsh-astra").delete();
            ServiceCqlShell.getInstance().install();
            Assertions.assertTrue(ServiceCqlShell.getInstance().isInstalled());
        }
    }

    @Test
    @Order(12)
    void testShouldStartShell() {
        if (!disableTools) {
            assertSuccessCli("db", "cqlsh", DB_TEST, "-e", "SELECT cql_version FROM system.local");
        }
    }


    @Test
    @Order(13)
    void testShouldThrowDatabaseAlreadyExist() {
        assertExitCodeCli(ExitCode.ALREADY_EXIST, "db create %s".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.ALREADY_EXIST, "db create-keyspace %s -k %s".formatted(DB_TEST, KEYSPACE_TEST));
    }

    @Test
    @Order(14)
    void testShouldThrowInvalidArgument()  {
        CliContext.getInstance().init(new CoreOptions(false,false,
                OutputFormat.HUMAN,
                AstraCliConfiguration.getDefaultConfigurationFileName()));
        CliContext.getInstance().initToken(new TokenOptions(null, AstraCliConfiguration.ASTRARC_DEFAULT, null));
        ServiceDatabase service = ServiceDatabase.getInstance();
        Assertions.assertThrows(InvalidArgumentException.class, () -> {
            service.generateDotEnvFile(DB_TEST, DB_TEST, "invalid", "/tmp");
        });
    }

    @Test
    @Order(15)
    void shouldDisplaySwaggerURLTest() {
        assertSuccessCli("db get-endpoint-swagger %s".formatted(DB_TEST));
    }

    @Test
    @Order(16)
    void shouldDisplayPlaygroundTest() {
        assertSuccessCli("db get-endpoint-playground %s".formatted(DB_TEST));
    }

    @Test
    @Order(17)
    void shouldListCdcTest() {
        // create and delete cdc will have dedicated test class as streaming is involved
        assertSuccessCli("db list-cdc %s".formatted(DB_TEST));
    }

    @Test
    @Order(18)
    void testShouldFailOnInvalidRegionAndCloud() throws DatabaseNameNotUniqueException {
        // When providing invalid region
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE,"db create invalid --region tropical-island");
        // When providing invalid cloud
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE,"db create invalid --cloud cumulonimbus --region us-east1");
        // When providing a region not in expected cloud (us-east1 is in gcp)
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE,"db create invalid2 --cloud aws --region us-east1");
    }

    @Test
    @Order(19)
    void testShouldTestException() {
        Assertions.assertThrows(DatabaseNameNotUniqueException.class, () -> {
            throw new DatabaseNameNotUniqueException(DB_TEST);
        });
    }

    @Test
    @Order(20)
    void testShouldListClouds() {
        assertSuccessCli("db list-clouds");
    }


    @Test
    @Order(21)
    void testShouldCreateAClassicAstra() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "db create cli5 --tier C20 --capacity-units 1 --if-not-exist");
    }

    @Test
    @Order(22)
    void shouldDisplayProperEndpoints () {
        assertSuccessCli("db get-endpoint-api %s".formatted(DB_TEST));
        assertSuccessCli("db get-endpoint-api %s".formatted(DB_TEST_VECTOR));
        assertSuccessCli("db get-endpoint-swagger %s".formatted(DB_TEST));
        assertSuccessCli("db get-endpoint-swagger %s".formatted(DB_TEST_VECTOR));
    }

    @Test
    @Order(22)
    void shouldCreateDotEnvWithVectorAndNotVector () {
        //assertSuccessCli("db create-dotenv %s".formatted(DB_TEST));
        assertSuccessCli("db create-dotenv %s".formatted(DB_TEST_VECTOR));
    }



}
