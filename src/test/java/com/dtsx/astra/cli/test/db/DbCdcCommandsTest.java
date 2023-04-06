package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import com.dtsx.astra.sdk.db.DatabaseClient;
import com.dtsx.astra.sdk.streaming.TenantClient;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * Test commands relative to CDC.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DbCdcCommandsTest extends AbstractCmdTest {

    /** Logger for my test. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DbCdcCommandsTest.class);

    static String DB_TEST       = "astra_cli_test";
    static String KEYSPACE_TEST = "ks1";
    static String TENANT_TEST   = "cdc" + UUID.randomUUID()
            .toString().replaceAll("-", "")
            .substring(0, 12);

    static DatabaseClient dbClient;
    static TenantClient   tenantClient;

    @BeforeAll
    public static void shouldInitializeEnvironment() {
        TENANT_TEST= "cdca8aefbb2690b";
        // Create DB
        //assertSuccessCli("db create %s -k %s --if-not-exist".formatted(DB_TEST, KEYSPACE_TEST));
        // Create TENANT
        assertSuccessCli("streaming create %s --if-not-exist".formatted(TENANT_TEST));
        // Create SCHEMA
        //assertSuccessCli("db cqlsh %s -f src/test/resources/cdc_dataset.cql".formatted(DB_TEST, KEYSPACE_TEST));
        // Access DbClient
        dbClient     = ctx().getApiDevopsDatabases().databaseByName(DB_TEST);
        tenantClient = ctx().getApiDevopsStreaming().tenant(TENANT_TEST);
   }

    @Test
    @Order(1)
    public void shouldCreateCdcs() throws InterruptedException {
        assertSuccessCli("db create-cdc %s -k %s --table demo --tenant %s -v".formatted(DB_TEST, KEYSPACE_TEST, TENANT_TEST));
        assertSuccessCli("db create-cdc %s -k %s --table table2 --tenant %s".formatted(DB_TEST, KEYSPACE_TEST, TENANT_TEST));
        Thread.sleep(1000);
        Assertions.assertEquals(2, dbClient.cdc().findAll().toList().size());
        Assertions.assertEquals(2, tenantClient.cdc().list());
    }

    @Test
    @Order(2)
    public void shouldInsertDataWithCdc() {
        assertSuccessCli("db cqlsh %s -k ks1 -e \"INSERT INTO demo(foo,bar) VALUES('2','2');\"".formatted(DB_TEST));
    }

    @Test
    @Order(3)
    public void shouldListCdcStreaming() {
       assertSuccessCli("streaming list-cdc %s".formatted(TENANT_TEST));
       assertSuccessCli("streaming list-cdc %s -o json".formatted(TENANT_TEST));
       assertSuccessCli("streaming list-cdc %s -o csv".formatted(TENANT_TEST));
   }

    @Test
    @Order(4)
    public void shouldListCdcFromDb() {
        assertSuccessCli("db list-cdc %s --no-color".formatted(DB_TEST));
    }

    @Test
    @Order(5)
    public void shouldDeleteCdcDbInvalid() {
        assertExitCodeCli(ExitCode.NOT_FOUND, "db delete-cdc %s -id invalid".formatted(DB_TEST));
    }

    @Test
    @Order(6)
    public void shouldDeleteCdcById() {
        // Given
        Assertions.assertEquals(2, dbClient.cdc().findAll().toList().size());
        Optional<CdcDefinition> cdc = dbClient.cdc().findByDefinition("ks1", "demo", TENANT_TEST);
        Assert.assertTrue(cdc.isPresent());
        // When (id is valid)
        Assert.assertTrue(dbClient.cdc().findById(cdc.get().getConnectorName()).isPresent());
        // When (delete by id)
        assertSuccessCli( "db delete-cdc %s -id %s".formatted(DB_TEST,  cdc.get().getConnectorName()));
        // Then
        Assertions.assertEquals(1, dbClient.cdc().findAll().toList().size());
    }

    @Test
    @Order(7)
    public void shouldDeleteCdcByDefinition() {
        // Given
        Assertions.assertEquals(1, dbClient.cdc().findAll().toList().size());
        // When
        assertSuccessCli( "db delete-cdc %s -k ks1 --table table2 --tenant %s".formatted(DB_TEST, TENANT_TEST));
        // Then
        Assertions.assertEquals(0, dbClient.cdc().findAll().toList().size());
    }

    @AfterAll
    public static void cleanUp() {
        //assertSuccessCli("streaming delete %s".formatted(TENANT_TEST));
    }

}
