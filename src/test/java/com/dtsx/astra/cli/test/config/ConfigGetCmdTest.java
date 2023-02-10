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
public class ConfigGetCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void should_get_config() {
        // Given
        assertSuccessCli("config create test-cli -v -t " + getToken());
        Assertions.assertFalse(config().getSection("test-cli").isEmpty());
        // When
        assertSuccessCli("config get test-cli -v");
        assertSuccessCli("config describe test-cli -v");
    }
    
    @Test
    @Order(2)
    public void should_get_fail_invalidsection() {
        // Given
        Assertions.assertTrue(config().getSection("does-not-exist").isEmpty());
        // When
        assertExitCodeCli(ExitCode.CONFIGURATION, "config get does-not-exist -v");
    }
}
