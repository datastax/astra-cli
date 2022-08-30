package com.datastax.astra;

import org.junit.jupiter.api.Test;

/**
 * Tests commands relative to config.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class SetupCommandsTest extends AbstractAstraCliTest {
    
    @Test
    public void should_display_version()  throws Exception {
        astraCli("--version");
    }
    
    @Test
    public void should_display_config()  throws Exception {
        astraCli("config", "list");
    }
    
    @Test
    public void should_display_default()  throws Exception {
        astraCli("config", "get", "default");
    }
    
    @Test
    public void should_display_default_key()  throws Exception {
        astraCli("config", "get", "default", "-k", "ASTRA_DB_APPLICATION_TOKEN");
    }
    
    @Test
    public void should_display_default_key_invalid()  throws Exception {
        astraCli("config", "get", "default", "-k", "INVALID");
    }
    
    @Test
    public void invalid_command()  throws Exception {
        astraCli("config", "create", "aaa", "-s", "INVALID");
    }
    
    @Test
    public void should_asktoken()  throws Exception {
        astraCli("setup");
    }
    
    @Test
    public void should_set_default_org()  throws Exception {
        astraCli("config", "default", "celphys@gmail.com");
    }
    
    @Test
    public void should_delete_config()  throws Exception {
        astraCli("config", "delete", "celpjus@gmail.com");
    }
    
    @Test
    public void should_show_config()  throws Exception {
        astraCli("config", "get", "default");
    }
    
    @Test
    public void should_show_help()  throws Exception {
        astraCli("help", "config");
    }
    
    @Test
    public void should_create()  throws Exception {
        astraCli("config", "create", "newSection", "-t", "AstraCS:TQPxCsTNLcAuPpuAcrCITtgq:5367eb28d1710199c6411a2ee20cb45d26104b8e32cd384c7e11c27ffa23d4a0");
    }

}
