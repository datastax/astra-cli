package com.datastax.astra.cli.test.db;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.core.out.OutputFormat;
import com.datastax.astra.cli.db.DbListCmd;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class DbListCmdTest extends AbstractCmdTest {
    
    /**
     * Init Command.
     * 
     * @return
     *      current command
     */
    private DbListCmd cmd() {
        return new DbListCmd().exit(false);
    }
    
    @Test
    @Order(1)
    public void should_list_db() {
        assertOK(cmd());
    }
    
    @Test
    @Order(2)
    public void should_list_db_verbose() {
        assertOK(cmd().verbose());
    }
    
    @Test
    @Order(3)
    public void should_list_db_nocolor() {
        assertOK(cmd().noColor());
    }
    
    @Test
    @Order(3)
    public void should_list_db_csv() 
    throws Exception {
        assertOK(cmd().output(OutputFormat.csv));
    }
    
    @Test
    @Order(4)
    public void should_list_db_json() 
    throws Exception {
        assertOK(cmd().output(OutputFormat.json));
    }
}
