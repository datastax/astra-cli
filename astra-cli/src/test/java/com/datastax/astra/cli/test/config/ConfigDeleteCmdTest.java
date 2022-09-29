package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
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
public class ConfigDeleteCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void should_delete_config() {
        // Given
        assertSuccessCli("config create test-cli -v -t " + getToken());
        Assertions.assertNotNull(astraRc().getSection("test-cli"));
        // When
        assertSuccessCli("config delete test-cli -v");
        // Then
        Assertions.assertNull(astraRc().getSection("test-cli"));
    }
    
    @Test
    @Order(2)
    public void should_delete_fail_with_invalidsection() {
        // Given and invalid section
        Assertions.assertNull(astraRc().getSection("test-cli"));
        // When
        assertExitCodeCli(ExitCode.CONFIGURATION, "config delete test-cli -v");
    }
    
}
