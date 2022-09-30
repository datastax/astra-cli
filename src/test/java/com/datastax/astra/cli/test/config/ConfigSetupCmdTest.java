package com.datastax.astra.cli.test.config;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.test.AbstractCmdTest;
import com.datastax.astra.cli.utils.AstraRcUtils;
import com.datastax.astra.sdk.config.AstraClientConfig;

/**
 * Tests commands relative to config.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class ConfigSetupCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void should_create_with_user_input()  throws Exception {
        // Given
        System.setIn(new ByteArrayInputStream((getToken() + "\n").getBytes()));
        // When
        assertSuccessCli("setup");
        // Then
        Assertions.assertNotNull(
                astraRc().getSection(AstraRcUtils.ASTRARC_DEFAULT));
        Assertions.assertEquals(
                getToken(),
                astraRc().getSection(AstraRcUtils.ASTRARC_DEFAULT)
                         .get(AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN));
    }
    
    @Test
    @Order(2)
    public void should_create_with_tokenParam()  throws Exception {
        // When
        assertSuccessCli("setup --token " + getToken());
        // Thebn
        Assertions.assertNotNull(
                astraRc().getSection(AstraRcUtils.ASTRARC_DEFAULT));
        Assertions.assertEquals(
                getToken(),
                astraRc().getSection(AstraRcUtils.ASTRARC_DEFAULT)
                         .get(AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN));
        
    }
}
