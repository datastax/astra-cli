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

}
