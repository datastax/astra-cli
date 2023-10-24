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
class ConfigCreateCmdTest extends AbstractCmdTest {
    
    @Test
    void should_create_config() {
        // Given
        assertSuccessCli("config create test-cli -t " + getToken());
        // When
        Assertions.assertFalse(config().getSection("test-cli").isEmpty());
    }

    @Test
    void should_create_config_withQuotes() {
        assertSuccessCli("config create test-cli-quotes -t \"" + getToken() + "\"");
    }

    @Test
    void shouldThrowErrorAsCannotConnectToDev() {
        assertExitCodeCli(ExitCode.CONFIGURATION, "config create error --env DEV --token " + getToken());
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "config create error --env INVALID --token " + getToken());
    }

    @Test
    void shouldCreateConfigWithProd() {
        assertSuccessCli( "config create ok --env PROD --token " + getToken());
        assertSuccessCli( "config delete ok");
    }

    @Test
    void should_not_create_config() {
        assertExitCodeCli(ExitCode.CONFIGURATION, "config create demo");
        assertExitCodeCli(ExitCode.CONFIGURATION, "config create demo -t invalid");
    }

}
