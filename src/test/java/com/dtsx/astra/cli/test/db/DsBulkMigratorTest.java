package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.db.migration.ServiceDsBulkMigrator;
import com.dtsx.astra.cli.streaming.pulsarshell.PulsarShellUtils;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import com.dtsx.astra.cli.test.streaming.PulsarShellTest;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to test specific DsBulk Migrator Commands
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DsBulkMigratorTest extends AbstractCmdTest {

    /** dataset. */
    public final static String DB_TEST       = "astra_cli_test";
    public final static String KEYSPACE_TEST = "astra_cli_test";

    /** Logger for my test. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DsBulkMigratorTest.class);

    /** Use to disable usage of CqlSh, DsBulkd and other during test for CI/CD. */
    public final static String FLAG_TOOLS = "disable_tools";

    /** flag coding for tool disabling. */
    public static boolean disableTools = false;

    @BeforeAll
    public static void shouldSetupTest() {
        readEnvVariable(FLAG_TOOLS).ifPresent(flag -> disableTools = Boolean.parseBoolean(flag));
        if (!disableTools) {
            LOGGER.info("Third party tools are enabled in test");
        }
    }

    @Test
    @Order(1)
    public void shoudInstallDsBulkMigrator()  {
        if (disableTools) {
            LOGGER.warn("Third Party tool is disabled for this test environment");
        } else {
            ServiceDsBulkMigrator.getInstance().install();
            Assertions.assertTrue(ServiceDsBulkMigrator.getInstance().isInstalled());
        }
    }

    @Test
    @Order(2)
    public void shouldGenerateDdlTest() {
        assertSuccessCli("db generate-ddl %s -k %s".formatted(DB_TEST, KEYSPACE_TEST));
    }

}
