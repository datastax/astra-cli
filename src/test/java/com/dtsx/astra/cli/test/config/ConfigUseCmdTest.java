 package com.dtsx.astra.cli.test.config;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
class ConfigUseCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    void should_use_config() {
        // Given
        assertSuccessCli("config create test-cli -v -t " + getToken());
        Assertions.assertFalse(config().getSection("test-cli").isEmpty());
        // When
        assertSuccessCli("config use test-cli -v");
    }
    
    @Test
    @Order(2)
    void shouldGetFailInvalidSection() {
        // Given
        Assertions.assertTrue(config().getSection("does-not-exist").isEmpty());
        // When
        assertExitCodeCli(ExitCode.CONFIGURATION, "config use does-not-exist -v");
    }
}
