package com.datastax.astra.streaming;

import org.junit.jupiter.api.Test;

import com.datastax.astra.AbstractAstraCliTest;

/**
 * Testing Streaming commands
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class StreamingCommandTest extends AbstractAstraCliTest {
    
    @Test
    public void should_show_help() {
        astraCli("help", "streaming");
    }
    
    @Test
    public void should_show_help_crete() {
        astraCli("help", "streaming", "create");
    }
    
    @Test
    public void should_create_tenant() {
        astraCli("streaming", "create", "cedrick-20220911");
    }
    
    @Test
    public void should_list_tenant() {
        astraCli("streaming", "list");
    }
    
    @Test
    public void should_get_tenant() {
        astraCli("streaming", "get", "trollsquad-2022");
    }
    
    @Test
    public void should_get_tenant_cloud() {
        astraCli("streaming", "get", "trollsquad-2022", "--key", "cloud");
    }
    
    @Test
    public void should_get_tenant_pulsar_token() {
        astraCli("streaming", "pulsar-token", "trollsquad-2022");
    }
    
    @Test
    public void should_get_status() {
        astraCli("streaming", "status", "trollsquad-2022");
    }
    
    @Test
    public void should_delete_tenant() {
        astraCli("streaming", "delete", "cedrick-20220911");
    }
    
    @Test
    public void should_exist_tenant() {
        astraCli("streaming", "exist", "cedrick-20220910");
    }
    

}
