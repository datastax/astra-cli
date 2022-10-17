package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.core.ExitCode;
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
        // Given
        assertSuccessCli("config create test-cli -v -t " + getToken());
        Assertions.assertNotNull(config().getSection("test-cli"));
        // When
        assertSuccessCli("config get test-cli -v");
    }
    
    @Test
    @Order(2)
    public void should_get_fail_invalidsection() {
        // Given
        Assertions.assertNull(config().getSection("does-not-exist"));
        // When
        assertExitCodeCli(ExitCode.CONFIGURATION, "config get does-not-exist -v");
    }
}
