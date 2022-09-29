package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.config.ConfigCreateCmd;
import com.datastax.astra.cli.config.ConfigUseCmd;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class ConfigUseCmdT extends AbstractCmdTest {
    
    @Test
    @Order(1)
    @DisplayName("Use with existing section")
    public void should_get_config() {
        // Given a config
        ConfigCreateCmd createCmd = new ConfigCreateCmd().verbose();
        createCmd.sectionName("test-cli").token(getToken()).runCmd();
        Assertions.assertNotNull(astraRc().getSection("test-cli"));
        // When
        ConfigUseCmd getCmd = new ConfigUseCmd().verbose();
        assertSuccess(getCmd.sectionName("test-cli"));
    }
    
    @Test
    @Order(2)
    @DisplayName("Use with invalid section")
    public void should_use_fail_invalidsection() {
        // Given
        ConfigUseCmd useCmd = new ConfigUseCmd().verbose();
        // When
        ExitCode code = useCmd.sectionName("does-not-exist").runCmd();
        //Then
        Assertions.assertNull(astraRc().getSection("does-not-exist"));
        Assertions.assertEquals(ExitCode.CONFIGURATION, code);
    }
}
