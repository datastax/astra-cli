package com.dtsx.astra.cli.test.streaming;

import java.util.UUID;

import com.dtsx.astra.cli.streaming.pulsarshell.PulsarShellUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Pulsar Shell.S
 *
 * @author Cedrick LUNVEN (@clunven)
 */
class PulsarShellTest extends AbstractCmdTest {
    
    /** Logger for my test. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PulsarShellTest.class);
    
    /** Use to disable usage of CqlSh, DsBulk and other during test for CI/CD. */
    public final static String FLAG_TOOLS = "disable_tools";
    
    static String RANDOM_TENANT = "cli-" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
    
    /** flag coding for tool disabling. */
    public static boolean disableTools = false;
    
    @BeforeAll
    static void should_create_when_needed() {
        readEnvVariable(FLAG_TOOLS).ifPresent(flag -> disableTools = Boolean.parseBoolean(flag));
        if (!disableTools) {
            LOGGER.info("Third party tools are enabled in test");
        }
        assertSuccessCli("streaming create " + RANDOM_TENANT);
    }
    
    @Test
    @Order(1)
    void shouldInstallPulsarShell()  {
        if (disableTools) {
            LOGGER.warn("Third Party tool is disabled for this test environment");
        } else {
            PulsarShellUtils.installPulsarShell();
            Assertions.assertTrue(PulsarShellUtils.isPulsarShellInstalled());
        }
    }
    
    @Test
    @Order(2)
    void should_pulsar_shell() {
        if (disableTools) {
            LOGGER.warn("Third Party tool is disabled for this test environment");
        } else {
            assertSuccessCli(new String[] {
                "streaming", "pulsar-shell", RANDOM_TENANT, "-e", 
                "admin namespaces list " + RANDOM_TENANT});
        }
    }
    
    @AfterAll
    static void should_close_tenant() {
        assertSuccessCli("streaming delete " + RANDOM_TENANT);
    }

}
