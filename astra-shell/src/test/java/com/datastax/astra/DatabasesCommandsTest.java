package com.datastax.astra;

import org.junit.jupiter.api.Test;

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
    public void createDb()  throws Exception {
        astraCli("db", "create", "test", "-r", "eu-central-1", "-ks", "ks1");
    }
    
    @Test
    public void getDb()  throws Exception {
        astraCli("db", "get","foo");
    }
    
    @Test
    public void cqlSHDB()  throws Exception {
        astraCli("db", "cqlsh", "foo", "-v");
    }
    
    @Test
    public void createKeyspace()  throws Exception {
        astraCli("db", "create-keyspace", "foo", "-k", "ks3", "-v");
    }
    
    @Test
    public void errorMessages() throws Exception {
        astraCli("xxx");
    }
    
    
    
}
