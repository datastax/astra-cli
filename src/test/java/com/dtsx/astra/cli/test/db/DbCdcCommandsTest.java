package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.streaming.TenantClient;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Optional;
import java.util.UUID;

/**
 * Test commands relative to CDC.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DbCdcCommandsTest  extends AbstractCmdTest {
    
    static String RANDOM_TENANT = "cdc" + UUID.randomUUID()
            .toString().replaceAll("-", "")
            .substring(0, 12);

    static DbOpsClient dbClient;
    static TenantClient  tenantClient;

    @BeforeAll
    static void shouldInitializeEnvironment() {
        // Create DB and keyspace with
        assertSuccessCli("db create %s --if-not-exist".formatted(DB_TEST));
        // Create TENANT
        assertSuccessCli("streaming create %s --cloud gcp --region useast1 --if-not-exist".formatted(RANDOM_TENANT));
        // Create SCHEMA
        assertSuccessCli("db cqlsh %s -f src/test/resources/cdc_dataset.cql".formatted(DB_TEST));
        // Access DbClient
        dbClient     = ctx().getApiDevopsDatabases().databaseByName(DB_TEST);
        tenantClient = ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT);
   }

    @Test
    @Order(1)
    void shouldCreateCdcs() throws InterruptedException {
        assertSuccessCli("db create-cdc %s -k %s --table demo --tenant %s -v".formatted(DB_TEST, DB_TEST, RANDOM_TENANT));
        assertSuccessCli("db create-cdc %s -k %s --table table2 --tenant %s".formatted(DB_TEST, DB_TEST, RANDOM_TENANT));
        Assertions.assertEquals(2L, dbClient.cdc().findAll().count());
        Assertions.assertEquals(2L, tenantClient.cdc().list().count());
    }

    @Test
    @Order(2)
    void shouldInsertDataWithCdc() {
        if (!disableTools) {
            assertSuccessCli("db cqlsh %s -f src/test/resources/cdc_insert.cql".formatted(DB_TEST));
        }
    }

    @Test
    @Order(3)
    void shouldListCdcStreaming() {
       assertSuccessCli("streaming list-cdc %s".formatted(RANDOM_TENANT));
       assertSuccessCli("streaming list-cdc %s -o json".formatted(RANDOM_TENANT));
       assertSuccessCli("streaming list-cdc %s -o csv".formatted(RANDOM_TENANT));
   }

    @Test
    @Order(4)
    void shouldListCdcFromDb() {
        assertSuccessCli("db list-cdc %s --no-color".formatted(DB_TEST));
    }

    @Test
    @Order(5)
    void shouldDeleteCdcDbInvalid() {
        assertExitCodeCli(ExitCode.NOT_FOUND, "db delete-cdc %s -id invalid".formatted(DB_TEST));
    }

    @Test
    @Order(6)
    void shouldDeleteCdcById() {
        // Given
        Assertions.assertEquals(2, dbClient.cdc().findAll().toList().size());
        Optional<CdcDefinition> cdc = dbClient.cdc().findByDefinition(DB_TEST, "demo", RANDOM_TENANT);
        Assertions.assertTrue(cdc.isPresent());
        // When (id is valid)
        Assertions.assertTrue(dbClient.cdc().findById(cdc.get().getConnectorName()).isPresent());
        // When (delete by id)
        assertSuccessCli( "db delete-cdc %s -id %s".formatted(DB_TEST,  cdc.get().getConnectorName()));
        // Then
        Assertions.assertEquals(1, dbClient.cdc().findAll().toList().size());
    }

    @Test
    @Order(7)
    void shouldDeleteCdcByDefinition() {
        // Given
        Assertions.assertEquals(1, dbClient.cdc().findAll().toList().size());
        // When
        assertSuccessCli( "db delete-cdc %s -k %s --table table2 --tenant %s".formatted(DB_TEST, DB_TEST, RANDOM_TENANT));
        // Then
        Assertions.assertEquals(0, dbClient.cdc().findAll().toList().size());
    }

    @AfterAll
    static void cleanUp() {
       assertSuccessCli("streaming delete %s".formatted(RANDOM_TENANT));
    }

}
