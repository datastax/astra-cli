package com.datastax.astra.cli.test.db;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.astra.cli.db.dsbulk.DsBulkService;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Datstax Bulk Loader.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DsBulkTest extends AbstractCmdTest {
    
    /** Logger for my test. */
    private static Logger LOGGER = LoggerFactory.getLogger(DsBulkTest.class);
   
    public final static String DB_TEST = "astra_cli_test";
    
    /** Use to disable usage of CqlSh, DsBulkd and other during test for CI/CD. */
    public final static String FLAG_TOOLS = "disable_tools";
    
    /** flag coding for tool disabling. */
    public static boolean disableTools = false;
    
    /** dataset. */
    public final static String TABLE_TEST = "test_dsbulk";
    
    @BeforeAll
    public static void should_create_when_needed() {
        readEnvVariable(FLAG_TOOLS).ifPresent(flag -> disableTools = Boolean.valueOf(flag));
        if (!disableTools) {
            LOGGER.info("Third party tools are enabled in test");
        }
    }
    
    @Test
    @Order(1)
    public void should_install_dsbulk()  throws Exception {
        if (disableTools) {
            LOGGER.warn("Third Party tool is disabled for this test environment");
        } else {
            DsBulkService.getInstance().install();
            Assertions.assertTrue(DsBulkService.getInstance().isInstalled());
        }
    }
    
    @Test
    @Order(2)
    public void testShould_count() {
        if (disableTools) {
            LOGGER.warn("Third Party tool is disabled for this test environment");
        } else {
            // Given
            assertSuccessCli("db", "create", DB_TEST, "--if-not-exists", "--wait");
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
    @Order(3)
    public void testShould_import() {
        if (disableTools) {
            LOGGER.warn("Third Party tool is disabled for this test environment");
        } else {
            // When
            assertSuccessCli("db", "load", DB_TEST, 
                    "-k", DB_TEST,
                    "-t", TABLE_TEST, 
                    "-url", "src/test/resources/test_dataset.csv", 
                    "-logDir", "/tmp");
        }   
    }
    
    @Test
    @Order(4)
    public void testShould_export() {
        if (disableTools) {
            LOGGER.warn("Third Party tool is disabled for this test environment");
        } else {
            // When
            assertSuccessCli("db", "unload", DB_TEST, "-k", DB_TEST,"-t", TABLE_TEST, 
                    "-url", "/tmp/export-"+ DB_TEST + "-" + TABLE_TEST,
                    "-logDir", "/tmp");
            Assertions.assertTrue(new File("/tmp/export-"+ DB_TEST + "-" + TABLE_TEST).exists());
        }   
    }

}
