package com.datastax.astra.cli.test.core;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test Stateless CLI.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class HelpCommandsTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void should_show_help() {
        astraCli("help");
    }
    
    
    @Test
    @Order(2)
    public void should_show_help_db() {
        astraCli("help db");
    }
    
    @Test
    @Order(3)
    public void should_show_help_db_create() {
        astraCli("help db create");
    }
    
}
