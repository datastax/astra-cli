package com.datastax.astra.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.datastax.astra.AbstractAstraCliTest;
import com.datastax.astra.shell.utils.DsBulkUtils;

/**
 * Tests DSBulk.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class DsbulkTests extends AbstractAstraCliTest {
    
    @Test
    public void should_install_DsBulk()  throws Exception {
        DsBulkUtils.installDsBulk();
        Assertions.assertTrue(DsBulkUtils.isDsBulkInstalled());
    }
    
    @Test
    public void should_run() {
        astraCli("db", "dsbulk", "workshops", 
                "load",
                "-url", "/Users/cedricklunven/Downloads/wh.csv",  
                "-k", "machine_learning",
                "-t", "book_by_id",
                "-delim", "|",
                "--schema.allowMissingFields", "true"
                );
    }

}
