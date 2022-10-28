package com.dtsx.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;

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
        // Given
        assertSuccessCli("config create test-cli -v -t " + getToken());
        Assertions.assertFalse(config().getSection("test-cli").isEmpty());
        // When
        assertSuccessCli("config delete test-cli -v");
        // Then
        Assertions.assertTrue(config().getSection("test-cli").isEmpty());
    }
    
    @Test
    @Order(2)
    public void should_delete_fail_with_invalidsection() {
        // Given and invalid section
        Assertions.assertTrue(config().getSection("test-cli").isEmpty());
        // When
        assertExitCodeCli(ExitCode.CONFIGURATION, "config delete test-cli -v");
    }
    
}
