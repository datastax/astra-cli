package com.datastax.astra.cli.test.db;

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
public class DbListCmdTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void should_list_db() {
        assertSuccessCli("db list");
    }
    
    @Test
    @Order(2)
    public void should_list_db_verbose() {
        assertSuccessCli("db list -v");
    }
    
    @Test
    @Order(3)
    public void should_list_db_nocolor() {
        assertSuccessCli("db list --no-color");
    }
    
    @Test
    @Order(4)
    public void should_list_db_csv() {
        assertSuccessCli("db list -o csv");
    }
    
    @Test
    @Order(5)
    public void should_list_db_json()  {
        assertSuccessCli("db list -o json");
    }
    
    @Test
    @Order(6)
    public void should_list_db_interactive(){
        assertSuccessInteractive("db list");
    }
}
