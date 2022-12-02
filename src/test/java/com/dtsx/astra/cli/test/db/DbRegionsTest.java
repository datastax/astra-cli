package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Crud on Db Region.
 */
public class DbRegionsTest extends AbstractCmdTest {

    /** make the test StandAlone. */
    private static String DB_TEST = "astra_cli_test";

    @BeforeAll
    public static void init() {
        assertSuccessCli("db create %s --if-not-exist --wait".formatted(DB_TEST));
    }

    @Test
    public void listRegionsTest() {
        assertSuccessCli("db list-regions %s".formatted(DB_TEST));
    }

    //@Test
    // Will take forever to run
    public void shouldAddRegionTest() {
        assertSuccessCli("db create-region %s -r eu-west-1 --async".formatted(DB_TEST));
    }

    //@Test
    // Will take forever to run
    public void deleteRegionTest() {
        assertSuccessCli("db delete-region%s -r eu-west-1 --async".formatted(DB_TEST));
    }


}
