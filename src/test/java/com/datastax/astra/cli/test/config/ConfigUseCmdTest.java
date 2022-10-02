package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class ConfigUseCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    @DisplayName("Use with existing section")
    public void should_get_config() {
        // Given a config
        assertSuccessCli("config create test-cli -t " + getToken());
        Assertions.assertNotNull(astraRc().getSection("test-cli"));
        // When
        assertSuccessCli("config use test-cli");
    }
    
    @Test
    @Order(2)
    @DisplayName("Use with invalid section")
    public void should_use_fail_invalidsection() {
        // Given
        Assertions.assertNull(astraRc().getSection("does-not-exist"));
        // Then
        assertExitCodeCli(ExitCode.CONFIGURATION, "config use does-not-exist");
    }
}
