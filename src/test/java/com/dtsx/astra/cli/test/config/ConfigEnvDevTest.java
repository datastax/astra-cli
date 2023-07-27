package com.dtsx.astra.cli.test.config;

import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Create Tests Against Astra Dev and Astra Test
 */
class ConfigEnvDevTest extends AbstractCmdTest  {

    @Test
    void shouldWorkWithDev() {
        String configDev = "dev-" + UUID.randomUUID().toString().replaceAll("-", "").substring(0,5);
        // Given
        Assertions.assertTrue(config().getSection(configDev).isEmpty());
        Assertions.assertNotNull(getTokenDev());
        // When
        assertSuccessCli("config create " + configDev + " --env DEV --token " + getTokenDev());
        Assertions.assertFalse(config().getSection(configDev).isEmpty());

        // --- Use Configuration ---

        // When
        assertSuccessCli("config list ");
        assertSuccessCli("config describe " + configDev);
        assertSuccessCli("db list --config " + configDev);
        assertSuccessCli("db list --token " + getTokenDev() + " --env DEV");

        // --- Delete Configuration ---

        // When
        assertSuccessCli("config delete " + configDev);
        // Then
        Assertions.assertTrue(config().getSection(configDev).isEmpty());
    }

    @Test
    void shouldWorkWithTest() {
        String configTest = "test-" + UUID.randomUUID().toString().replaceAll("-", "").substring(0,5);
        // Given
        Assertions.assertTrue(config().getSection(configTest).isEmpty());
        Assertions.assertNotNull(getTokenTest());
        // When
        assertSuccessCli("config create " + configTest + " --env TEST --token " + getTokenTest());
        Assertions.assertFalse(config().getSection(configTest).isEmpty());

        // --- Use Configuration ---

        // When
        assertSuccessCli("config list ");
        assertSuccessCli("config describe " + configTest);
        assertSuccessCli("db list --config " + configTest);
        assertSuccessCli("db list --token " + getTokenTest() + " --env TEST");

        // --- Delete Configuration ---

        // When
        assertSuccessCli("config delete " + configTest);
        // Then
        Assertions.assertTrue(config().getSection(configTest).isEmpty());
    }

}