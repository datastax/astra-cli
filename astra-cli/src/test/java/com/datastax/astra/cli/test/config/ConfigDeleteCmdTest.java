package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.config.ConfigCreateCmd;
import com.datastax.astra.cli.config.ConfigDeleteCmd;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class ConfigDeleteCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void should_delete_config() {
        // Given a config
        ConfigCreateCmd createCmd = new ConfigCreateCmd().verbose();
        createCmd.sectionName("test-cli").token(getToken()).runCmd();
        Assertions.assertNotNull(astraRc().getSection("test-cli"));
        // When
        ConfigDeleteCmd deleteCmd = new ConfigDeleteCmd().exit(false).verbose();
        assertOK(deleteCmd.sectionName("test-cli"));
        // Then
        Assertions.assertNull(astraRc().getSection("test-cli"));
    }
    
    @Test
    @Order(2)
    public void should_delete_fail_invalidsection() {
        // Given
        // When
        ConfigDeleteCmd deleteCmd = new ConfigDeleteCmd().exit(false).verbose();
        ExitCode code = deleteCmd.sectionName("does-not-exist").runCmd();
        Assertions.assertNull(astraRc().getSection("does-not-exist"));
        Assertions.assertEquals(ExitCode.CONFIGURATION, code);
    }
    
    
}
