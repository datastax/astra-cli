package com.dtsx.astra.cli.test.config;

import java.io.ByteArrayInputStream;

import com.dtsx.astra.cli.config.AstraConfiguration;
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
public class ConfigSetupCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void should_create_with_user_input()  {
        // Given
        System.setIn(new ByteArrayInputStream((getToken() + "\n").getBytes()));
        // When
        assertSuccessCli("setup");
        // Then
        Assertions.assertFalse(config().getSection(AstraConfiguration.ASTRARC_DEFAULT).isEmpty());
        Assertions.assertEquals(
                getToken(),
                config().getSection(AstraConfiguration.ASTRARC_DEFAULT)
                         .get(AstraRc.ASTRA_DB_APPLICATION_TOKEN));
    }
    
    @Test
    @Order(2)
    public void should_create_with_tokenParam() {
        // When
        assertSuccessCli("setup --token " + getToken());
        // Then
        Assertions.assertFalse(config().getSection(AstraConfiguration.ASTRARC_DEFAULT).isEmpty());
        Assertions.assertEquals(
                getToken(),
                config().getSection(AstraConfiguration.ASTRARC_DEFAULT)
                         .get(AstraRc.ASTRA_DB_APPLICATION_TOKEN));
        
    }

    @Test
    @Order(3)
    public void shoudReturnedExceptionTest() {
        assertExitCodeCli(ExitCode.CONFIGURATION, "setup --token stupid");
    }

}
