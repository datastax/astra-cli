package com.dtsx.astra.cli.test.config;

import org.junit.jupiter.api.Test;

import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
class ConfigListCmdTest extends AbstractCmdTest {
    
    @Test
    void should_list_config() {
        assertSuccessCli("config list");
        //assertSuccessCli("config list -v");
        //assertSuccessCli("config list --no-color");
        //assertSuccessCli("config list -o json");
        //assertSuccessCli("config list -o csv");
    }
}
