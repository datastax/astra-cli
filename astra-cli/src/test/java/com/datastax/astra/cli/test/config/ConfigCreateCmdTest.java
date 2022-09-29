package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class ConfigCreateCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void should_create_config() {
        assertSuccessCli("config create test-cli -t " + getToken());
        Assertions.assertNotNull(astraRc().getSection("test-cli"));
    }
}
