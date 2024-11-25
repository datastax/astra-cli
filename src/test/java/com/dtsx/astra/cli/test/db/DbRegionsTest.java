package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.dtsx.astra.cli.core.ExitCode.ALREADY_EXIST;
import static com.dtsx.astra.cli.core.ExitCode.NOT_FOUND;

/**
 * Crud on Db Region.
 */
class DbRegionsTest extends AbstractCmdTest {

    @BeforeAll
    static void initDb() {
        assertSuccessCli("db create %s --if-not-exist".formatted(DB_TEST));
    }

    @Test
    void listRegionsTest() {
        assertSuccessCli("db list-regions %s".formatted(DB_TEST));
    }

    @Test
    void shouldThrowRegionAlreadyExistTest() {
        assertExitCodeCli(ALREADY_EXIST, "db create-region %s -r us-east1 -c gcp".formatted(DB_TEST));
        
    }

    @Test
    void shouldThrowRegionNotFoundTest() {
        assertExitCodeCli(NOT_FOUND, "db delete-region %s -r eu-west-1".formatted(DB_TEST));
    }

}
