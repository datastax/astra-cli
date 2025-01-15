package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.dtsx.astra.cli.core.ExitCode.ALREADY_EXIST;
import static com.dtsx.astra.cli.core.ExitCode.NOT_FOUND;

/**
 * Crud on Db Region.
 */
class DbRegionsFixBugTest extends AbstractCmdTest {

    @Test
    void listRegionsTest() {
        assertSuccessCli("db list-regions %s".formatted("source_db"));
    }

    @Test
    void shouldThrowRegionAlreadyExistTest() {
        assertExitCodeCli(ALREADY_EXIST, "db create-region %s -r us-east-2".formatted("source_db"));
    }

}
