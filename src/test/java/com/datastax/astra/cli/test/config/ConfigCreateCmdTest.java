package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.test.AbstractCmdTest;

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
        Assertions.assertNotNull(astraRc().getSection("test-cli"));
    }
    
    @Test
    public void should_not_create_config() {
        assertExitCodeCli(ExitCode.NOT_FOUND, "config create demo");
        assertExitCodeCli(ExitCode.INVALID_PARAMETER, "config create demo -t invalid");
    }
}
