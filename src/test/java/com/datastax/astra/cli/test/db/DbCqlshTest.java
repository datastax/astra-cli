package com.datastax.astra.cli.test.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.astra.cli.db.cqlsh.CqlShellUtils;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Working with Cqlsh.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DbCqlshTest extends AbstractCmdTest {
    
    /** Logger for my test. */
    private static Logger LOGGER = LoggerFactory.getLogger(DbCqlshTest.class);
   
    public final static String DB_TEST = "astra_cli_test";
    
    /** Use to disable usage of CqlSh, DsBulkd and other during test for CI/CD. */
    public final static String FLAG_TOOLS = "disable_tools";
    
    /** flag coding for tool disabling. */
    public static boolean disableTools = false;
    
    @BeforeAll
    public static void should_create_when_needed() {
        readEnvVariable(FLAG_TOOLS).ifPresent(flag -> disableTools = Boolean.valueOf(flag));
        if (!disableTools) {
            LOGGER.info("Third party tools are enabled in test");
        }
    }
    
    @Test
    @Order(1)
    public void should_install_Cqlsh()  throws Exception {
        if (disableTools) {
            LOGGER.warn("Third Party tool is disabled for this test environment");
        } else {
            CqlShellUtils.installCqlShellAstra();
            Assertions.assertTrue(CqlShellUtils.isCqlShellInstalled());
        }
    }
    
}
