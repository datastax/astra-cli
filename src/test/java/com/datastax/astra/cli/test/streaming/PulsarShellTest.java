package com.datastax.astra.cli.test.streaming;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.astra.cli.streaming.pulsarshell.PulsarShellUtils;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Pulsar Shell.S
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class PulsarShellTest extends AbstractCmdTest {
    
    /** Logger for my test. */
    private static Logger LOGGER = LoggerFactory.getLogger(PulsarShellTest.class);
    
    /** Use to disable usage of CqlSh, DsBulkd and other during test for CI/CD. */
    public final static String FLAG_TOOLS = "disable_tools";
    
    static String RANDOM_TENANT = "cli-" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
    
    /** flag coding for tool disabling. */
    public static boolean disableTools = false;
    
    @BeforeAll
    public static void should_create_when_needed() {
        readEnvVariable(FLAG_TOOLS).ifPresent(flag -> disableTools = Boolean.valueOf(flag));
        if (!disableTools) {
            LOGGER.info("Third party tools are enabled in test");
        }
        assertSuccessCli("streaming create " + RANDOM_TENANT);
    }
    
    @Test
    @Order(1)
    public void should_install_pulsarshell()  throws Exception {
        if (disableTools) {
            LOGGER.warn("Third Party tool is disabled for this test environment");
        } else {
            PulsarShellUtils.installPulsarShell();
            Assertions.assertTrue(PulsarShellUtils.isPulsarShellInstalled());
        }
    }
    
    @Test
    @Order(2)
    public void should_pulsar_shell() {
        assertSuccessCli(new String[] {
                "streaming", "pulsar-shell", RANDOM_TENANT, "-e", 
                "admin namespaces list " + RANDOM_TENANT});
    }
    
    @AfterAll
    public static void should_close_tenant() {
        assertSuccessCli("streaming delete " + RANDOM_TENANT);
    }

}
