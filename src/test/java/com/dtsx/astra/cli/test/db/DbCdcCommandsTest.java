package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.db.cqlsh.ServiceCqlShell;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import com.dtsx.astra.sdk.db.DatabaseClient;
import com.dtsx.astra.sdk.streaming.TenantClient;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Test commands relative to CDC.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DbCdcCommandsTest extends AbstractCmdTest {

    static String DB_TEST     = "astra_cli_test_cdc";
    static String TENANT_TEST = "cdc" + UUID.randomUUID().toString().replaceAll("-", "")
            .substring(0, 12);

    static DatabaseClient dbClient;
    static TenantClient   tenantClient;

    @BeforeAll
    public static void shouldInitializeEnvironment() {
        // Create DB
        assertSuccessCli("db create %s -k ks1 --if-not-exist".formatted(DB_TEST));
        // Create TENANT
        assertSuccessCli("streaming create %s --if-not-exist".formatted(TENANT_TEST));
        // Create SCHEMA
        assertSuccessCli(("db cqlsh %s -k ks1 -e \"" +
                "CREATE TABLE demo(foo text PRIMARY KEY, bar text);" +
                "CREATE TABLE table2(foo text PRIMARY KEY, bar text);\"")
                .formatted(DB_TEST));
        // Insert Data
        assertSuccessCli("db cqlsh %s -k ks1 -e \"INSERT INTO demo(foo,bar) VALUES('1','1');\"".formatted(DB_TEST));
        // Access DbClient
        dbClient     = ctx().getApiDevopsDatabases().name(DB_TEST);
        tenantClient = ctx().getApiDevopsStreaming().tenant(TENANT_TEST);
   }

    @Test
    @Order(1)
    public void shouldCreateCdcs() throws InterruptedException {
        assertSuccessCli("db create-cdc %s -k ks1 --table demo --tenant %s".formatted(DB_TEST, TENANT_TEST));
        assertSuccessCli("db create-cdc %s -k ks1 --table table2 --tenant %s".formatted(DB_TEST, TENANT_TEST));
        Thread.sleep(1000);
        Assertions.assertEquals(2, dbClient.cdcs().toList().size());
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
        Assertions.assertEquals(2, dbClient.cdcs().toList().size());
        Optional<CdcDefinition> cdc = dbClient.findCdcByDefinition("ks1", "demo", TENANT_TEST);
        Assert.assertTrue(cdc.isPresent());
        // When (id is valid)
        Assert.assertTrue(dbClient.findCdcById(cdc.get().getConnectorName()).isPresent());
        // When (delete by id)
        assertSuccessCli( "db delete-cdc %s -id %s".formatted(DB_TEST,  cdc.get().getConnectorName()));
        // Then
        Assertions.assertEquals(1, dbClient.cdcs().toList().size());
    }

    @Test
    @Order(7)
    public void shouldDeleteCdcByDefinition() {
        // Given
        Assertions.assertEquals(1, dbClient.cdcs().toList().size());
        // When
        assertSuccessCli( "db delete-cdc %s -k ks1 --table table2 --tenant %s".formatted(DB_TEST, TENANT_TEST));
        // Then
        Assertions.assertEquals(0, dbClient.cdcs().toList().size());
    }

    @AfterAll
    public static void cleanUp() {
        assertSuccessCli("db delete %s".formatted(DB_TEST));
        assertSuccessCli("streaming delete %s".formatted(TENANT_TEST));
    }

}
