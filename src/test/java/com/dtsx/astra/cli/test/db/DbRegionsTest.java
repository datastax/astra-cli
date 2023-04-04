package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.dtsx.astra.cli.core.ExitCode.ALREADY_EXIST;
import static com.dtsx.astra.cli.core.ExitCode.NOT_FOUND;

/**
 * Crud on Db Region.
 */
public class DbRegionsTest extends AbstractCmdTest {

    /** make the test StandAlone. */
    private static String DB_TEST = "astra_cli_test";

    @BeforeAll
    public static void init() {
        assertSuccessCli("db create %s --if-not-exist".formatted(DB_TEST));
    }

    @Test
    public void listRegionsTest() {
        assertSuccessCli("db list-regions %s".formatted(DB_TEST));
    }

    @Test
    public void shouldThrowRegionAlreadyExistTest() {
        assertExitCodeCli(ALREADY_EXIST, "db create-region %s -r us-east1".formatted(DB_TEST));
    }

    @Test
    public void shouldThrowRegionNotFoundTest() {
        assertExitCodeCli(NOT_FOUND, "db delete-region %s -r eu-west-1".formatted(DB_TEST));
    }

}
