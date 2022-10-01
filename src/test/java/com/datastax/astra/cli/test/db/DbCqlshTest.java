package com.datastax.astra.cli.test.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.db.cqlsh.CqlShellUtils;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Working with Cqlsh.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DbCqlshTest extends AbstractCmdTest {
    
    static String DB_TEST = "astra_cli_test";
    
    @BeforeAll
    public static void should_create_when_needed() {
        assertSuccessCli("db create %s --if-not-exist --wait".formatted(DB_TEST));
    }
    
    @Test
    @Order(1)
    public void should_install_Cqlsh()  throws Exception {
        CqlShellUtils.installCqlShellAstra();
        Assertions.assertTrue(CqlShellUtils.isCqlShellInstalled());
    }
    
}
