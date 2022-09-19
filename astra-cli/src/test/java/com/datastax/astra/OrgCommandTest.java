package com.datastax.astra;

import org.junit.jupiter.api.Test;

/**
 * Operation on org.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class OrgCommandTest extends AbstractAstraCliTest {
    
    @Test
    public void should_display_org()  throws Exception {
        astraCli("org");
    }
    
    @Test
    public void should_display_id()  throws Exception {
        astraCli("org", "id");
    }
    
    @Test
    public void should_display_name()  throws Exception {
        astraCli("org", "name");
    }
    
    @Test
    public void should_display_classic()  throws Exception {
        astraCli("org", "list-regions-classic");
    }
    
    @Test
    public void should_display_serverless()  throws Exception {
        astraCli("org", "list-regions-serverless");
    }

}