package com.datastax.astra.db;

import org.junit.jupiter.api.Test;

import com.datastax.astra.AbstractAstraCliTest;

/**
 * Testing CRUD for databases.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabasesCommandsTest extends AbstractAstraCliTest {
    
    @Test
    public void showDbs()  throws Exception {
        astraCli("db", "list");
    }
    
    @Test
    public void createDbTest()  throws Exception {
        astraCli("db", "create", "test5", "--wait");
    }
    
    @Test
    public void resumeWithWait()  throws Exception {
        astraCli("db", "resume", "test4", "--wait");
    }
    
    @Test
    public void createDb()  throws Exception {
        astraCli("db", "create", "\"Feedly Clone\"", 
                    "-k", "test4", 
                    "--if-not-exist", "--wait");
    }
    
    @Test
    public void getStatus()  throws Exception {
        astraCli("db", "status","test4");
    }
    
    @Test
    public void getDb()  throws Exception {
        astraCli("db", "get","foo");
    }
    
    @Test
    public void cqlSHDB()  throws Exception {
        astraCli("db", "cqlsh", "foo");
    }
    
    @Test
    public void createKeyspace()  throws Exception {
        astraCli("db", "create-keyspace", "foo", "-k", "ks3", "-v");
    }
    
    @Test
    public void listKeyspaces()  throws Exception {
        astraCli("db", "list-keyspaces", "mtg");
    }
    
    
    @Test
    public void resumeDB()  throws Exception {
        astraCli("db", "resume", "samplesgallery", "--config", "greg");
    }
   
    
    @Test
    public void helpDL()  throws Exception {
        astraCli("help", "db", "download-scb");
    }
   
    
    @Test
    public void errorMessages() throws Exception {
        astraCli("xxx");
    }
    
    @Test
    public void getDbKey()  throws Exception {
        astraCli("db", "get","mtg", "--key", "region");
    }
    
    @Test
    public void helpDbGet()  throws Exception {
        astraCli("help","db", "get");
    }
    
}
