package com.dtsx.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class ConfigCreateCmdTest extends AbstractCmdTest {
    
    @Test
    public void should_create_config() {
        // Given
        assertSuccessCli("config create test-cli -t " + getToken());
        // When
        Assertions.assertFalse(config().getSection("test-cli").isEmpty());
    }
    
    @Test
    public void should_not_create_config() {
        assertExitCodeCli(ExitCode.CONFIGURATION, "config create demo");
        assertExitCodeCli(ExitCode.CONFIGURATION, "config create demo -t invalid");
    }
}
