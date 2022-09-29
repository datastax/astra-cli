package com.datastax.astra.cli.test.db;

import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Create dotenv file.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class DbCreateDotEnvTest extends AbstractCmdTest {

    @Test
    public void should_create_dot_env()  throws Exception {
        assertSuccessCli("db create-dotenv mtg");
    }
}
