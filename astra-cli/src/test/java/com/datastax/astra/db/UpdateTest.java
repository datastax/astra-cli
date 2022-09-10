package com.datastax.astra.db;

import org.junit.jupiter.api.Test;

import com.datastax.astra.AbstractAstraCliTest;

/**
 * Tests DSBulk.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class UpdateTest extends AbstractAstraCliTest {
    
    @Test
    public void should_update() {
        astraCli("update");
    }

}
