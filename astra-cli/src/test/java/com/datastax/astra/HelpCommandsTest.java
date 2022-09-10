package com.datastax.astra;

import org.junit.jupiter.api.Test;

/**
 * Test Stateless CLI.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class HelpCommandsTest extends AbstractAstraCliTest {
    
    @Test
    public void should_display_main_help() throws Exception {
        astraCli("help");
    }
    
    @Test
    public void should_display_help_show() throws Exception {
        astraCli("help", "show");
    }
    
    @Test
    public void should_display_help_create() throws Exception {
        astraCli("help", "create");
    }
    
    @Test
    public void should_display_help_delete() throws Exception {
        astraCli("help", "delete");
    }
   
    
    @Test
    public void should_display_help_deletedb() throws Exception {
        astraCli("help", "delete", "db");
    }
   
}
