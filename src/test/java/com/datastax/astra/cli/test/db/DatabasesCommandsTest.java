package com.datastax.astra.cli.test.db;

import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Testing CRUD for databases.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabasesCommandsTest extends AbstractCmdTest {
    
    static String DB_TEST = "astra-cli-test";
    
    @Test
    public void invalidCommand ()  throws Exception {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "titi invalid");
    }
    
    /*
    
    @Test
    public void showDbs()  throws Exception {
        assertSuccessCli("db list");
    }
    
    @Test
    public void createDbTest()  throws Exception {
        assertSuccessCli("db create test5 --wait");
    }
    
    @Test
    public void resumeWithWait()  throws Exception {
        assertSuccessCli("db resume test4 --wait");
    }
    
    @Test
    public void createDb()  throws Exception {
        assertSuccessCli("db create \"Feedly Clone\" -k test4 --if-not-exist --wait");
    }
    
    @Test
    public void getStatus()  throws Exception {
        assertSuccessCli("db status test4");
    }
    
    @Test
    public void getDb()  throws Exception {
        assertSuccessCli("db get foo");
    }
    
    @Test
    public void cqlSHDB()  throws Exception {
        assertSuccessCli("db cqlsh foo");
    }
    
    @Test
    public void createKeyspace()  throws Exception {
        assertSuccessCli("db create-keyspace foo -k ks3 -v");
    }
    
    @Test
    public void listKeyspaces()  throws Exception {
        assertSuccessCli("db list-keyspaces mtg");
    }
    
    
    @Test
    public void resumeDB()  throws Exception {
        assertSuccessCli("db resume samplesgallery --config greg");
    }
   
    
    @Test
    public void helpDL()  throws Exception {
        assertSuccessCli("help db download-scb");
    }
   
    
    @Test
    public void errorMessages() throws Exception {
        assertSuccessCli("xxx");
    }
    
    @Test
    public void getDbKey()  throws Exception {
        assertSuccessCli("db get mtg --key region");
    }
    
    @Test
    public void helpDbGet()  throws Exception {
        assertSuccessCli("help db get");
    }*/
    
}
