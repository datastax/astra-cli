package com.dtsx.astra.cli.test.config;

import java.io.ByteArrayInputStream;

import com.dtsx.astra.cli.config.AstraCliConfiguration;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.sdk.utils.AstraRc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Tests commands relative to config.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
class ConfigSetupCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    void should_create_with_user_input()  {
        // Given
        System.setIn(new ByteArrayInputStream((getToken() + "\n").getBytes()));
        // When
        assertSuccessCli("setup");
        // Then
        validateSection();
    }
    
    @Test
    @Order(2)
    void shouldCreateWithTokenParam() {
        // When
        assertSuccessCli("setup --token " + getToken());
        // Then
        validateSection();
        
    }

    @Test
    @Order(3)
    void shouldReturnedExceptionTest() {
        assertExitCodeCli(ExitCode.CONFIGURATION, "setup --token stupid");
    }

    @Test
    @Order(4)
    void should_create_with_user_login()  {
        // Given
        System.setIn(new ByteArrayInputStream((getToken() + "\n").getBytes()));
        // When
        assertSuccessCli("login");
        // Then
        validateSection();
    }

    @Test
    @Order(5)
    void shouldLoginWithToken() {
        // When
        assertSuccessCli("login --token " + getToken());
        // Then
        validateSection();
    }

    /**
     * Validate that user input is in the config file
     */
    private void validateSection() {
        Assertions.assertFalse(config().getSection(AstraCliConfiguration.ASTRARC_DEFAULT).isEmpty());
        Assertions.assertEquals(
                getToken(),
                config().getSection(AstraCliConfiguration.ASTRARC_DEFAULT)
                        .get(AstraRc.ASTRA_DB_APPLICATION_TOKEN));
    }

}
