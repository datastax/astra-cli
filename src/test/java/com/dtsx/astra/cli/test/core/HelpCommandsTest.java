package com.dtsx.astra.cli.test.core;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Test Stateless CLI.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
class HelpCommandsTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    void should_show_help() {
        assertSuccessCli("help");
    }
    
    
    @Test
    @Order(2)
    void should_show_help_db() {
        assertSuccessCli("help db");
    }
    
    @Test
    @Order(3)
    void should_show_help_db_create() {
        assertSuccessCli("help db create");
    }
    
}
