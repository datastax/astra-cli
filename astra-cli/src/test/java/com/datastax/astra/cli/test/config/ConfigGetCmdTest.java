package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.config.ConfigCreateCmd;
import com.datastax.astra.cli.config.ConfigGetCmd;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class ConfigGetCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void should_get_config() {
        // Given a config
        ConfigCreateCmd createCmd = new ConfigCreateCmd().verbose();
        createCmd.sectionName("test-cli").token(getToken()).runCmd();
        Assertions.assertNotNull(astraRc().getSection("test-cli"));
        // When
        ConfigGetCmd getCmd = new ConfigGetCmd().exit(false).verbose();
        assertOK(getCmd.sectionName("test-cli"));
    }
    
    @Test
    @Order(2)
    public void should_get_fail_invalidsection() {
        // Given
        // When
        ConfigGetCmd getCmd = new ConfigGetCmd().exit(false).verbose();
        ExitCode code = getCmd.sectionName("does-not-exist").runCmd();
        Assertions.assertNull(astraRc().getSection("does-not-exist"));
        Assertions.assertEquals(ExitCode.CONFIGURATION, code);
    }
}
