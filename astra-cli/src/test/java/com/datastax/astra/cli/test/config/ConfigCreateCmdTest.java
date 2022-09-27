package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.datastax.astra.cli.config.ConfigCreateCmd;
import com.datastax.astra.cli.test.AbstractCmdTest;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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
        ConfigCreateCmd cmd = new ConfigCreateCmd().exit(false).verbose();
        assertOK(cmd.sectionName("test-cli").token(getToken()));
        Assertions.assertNotNull(astraRc().getSection("test-cli"));
    }
}
