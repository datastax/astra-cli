package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.Test;

/**
 * Crud on Db Region.
 */
public class DbRegionsTest extends AbstractCmdTest {

    @Test
    public void listRegions() {
        assertSuccessCli("db list-regions workshops");
    }

    @Test
    public void deleteRegions() {
        assertSuccessCli("db delete-region workshops -r australiaeast --async");
    }

    @Test
    public void addRegion() {
        assertSuccessCli("db create-region data-modeling -r eu-west-1 --async");
    }

    @Test
    public void listDb() {
        assertSuccessCli("config get default");
    }


}
