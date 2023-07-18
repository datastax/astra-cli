package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.db.dsbulk.ServiceDsBulk;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;

/**
 * Class to test specific DsBulk Commands
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DbDsBulkTest extends AbstractCmdTest {

    /** dataset. */
    public final static String DB_TEST       = "astra_cli_test";
    public final static String KEYSPACE_TEST = "dsbulk";
    public final static String TABLE_TEST    = "cities_by_country";

    @BeforeAll
    public static void initForDsBulk() {
        // Create expected DB is not Exist
        assertSuccessCli("db create %s -k %s --if-not-exist".formatted(DB_TEST, KEYSPACE_TEST));
        // Create expected table
        assertSuccessCli("db cqlsh %s -f src/test/resources/test_schema.cql".formatted(DB_TEST));
    }

    @Test
    @Order(1)
    @DisplayName("Installing DsBulk")
    public void testShouldInstallDsbulk() {
        if (!disableTools) {
            // given
            if (new File(AstraCliUtils.ASTRA_HOME + File.separator
                    + "dsbulk-"
                    + AstraCliUtils.readProperty("dsbulk.version")).delete()) {
                System.out.println("Deleted old DsBulk");
            }
            // When
            ServiceDsBulk.getInstance().install();
            // Then
            Assertions.assertTrue(ServiceDsBulk.getInstance().isInstalled());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Load Data")
    public void testShouldLoad() {
        if (!disableTools){
            assertSuccessCli("db", "load", DB_TEST,
                    "-k", KEYSPACE_TEST,
                    "-t", TABLE_TEST,
                    "-url", "src/test/resources/test_dataset.csv",
                    "--schema.allowMissingFields", "true",
                    "-logDir", "/tmp");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Count Data")
    public void testShouldCount() {
        if (!disableTools) {

            // Sample with table name
            assertSuccessCli("db", "count", DB_TEST,
                    "-k", KEYSPACE_TEST,
                    "-t", TABLE_TEST,
                    "-logDir", "/tmp");

            // Sample with query
            assertSuccessCli("db", "count", DB_TEST,
                    "-k", KEYSPACE_TEST,
                    "-query", "SELECT * FROM " + TABLE_TEST + " WHERE country_name='France'",
                    "-logDir", "/tmp");

        }
    }

    @Test
    @Order(4)
    @DisplayName("Unload Data")
    public void testShouldUnload() {
        if (!disableTools) {
            assertSuccessCli("db", "unload", DB_TEST,
                    "-k", KEYSPACE_TEST,
                    "-t", TABLE_TEST,
                    "-url", "/tmp/export-"+ KEYSPACE_TEST + "-" + TABLE_TEST,
                    "-logDir", "/tmp");


            assertSuccessCli("db", "unload", DB_TEST,
                    "-k", KEYSPACE_TEST,
                    "-url", "/tmp/export-"+ DB_TEST + "-" + TABLE_TEST,
                    "-query", "SELECT * FROM " + TABLE_TEST + " WHERE country_name='France'",
                    "-logDir", "/tmp");
        }
    }
}
