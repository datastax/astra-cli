package com.datastax.astra.cli.test.config;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.config.ConfigListCmd;
import com.datastax.astra.cli.core.out.OutputFormat;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class ConfigListCmdTest extends AbstractCmdTest {
    
    /**
     * Init Command.
     * 
     * @return
     *      current command
     */
    private ConfigListCmd cmd() {
        return new ConfigListCmd().exit(false);
    }
    
    @Test
    @Order(1)
    public void should_list_config() {
        assertOK(cmd());
    }
    
    @Test
    @Order(2)
    public void should_list_config_verbose() {
        assertOK(cmd().verbose());
    }
    
    @Test
    @Order(3)
    public void should_list_config_nocolor() {
        assertOK(cmd().noColor());
    }
    
    @Test
    @Order(3)
    public void should_list_config_csv() 
    throws Exception {
        assertOK(cmd().output(OutputFormat.csv));
    }
    
    @Test
    @Order(4)
    public void should_list_config_json() 
    throws Exception {
        assertOK(cmd().output(OutputFormat.json));
    }
}
