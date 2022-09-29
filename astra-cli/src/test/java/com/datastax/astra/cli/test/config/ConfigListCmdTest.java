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
    
    @Test
    @Order(1)
    public void should_list_config() {
        assertSuccess(new ConfigListCmd());
    }
    
    @Test
    @Order(2)
    public void should_list_config_verbose() {
        assertSuccess(new ConfigListCmd().verbose());
    }
    
    @Test
    @Order(3)
    public void should_list_config_nocolor() {
        assertSuccess(new ConfigListCmd().noColor());
    }
    
    @Test
    @Order(3)
    public void should_list_config_csv() 
    throws Exception {
        assertSuccess(new ConfigListCmd().output(OutputFormat.csv));
    }
    
    @Test
    @Order(4)
    public void should_list_config_json() 
    throws Exception {
        assertSuccess(new ConfigListCmd().output(OutputFormat.json));
    }
}
