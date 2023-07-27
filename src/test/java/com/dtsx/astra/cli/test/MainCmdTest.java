package com.dtsx.astra.cli.test;

import org.junit.jupiter.api.Test;

/**
 * Test Main behaviour.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
class MainCmdTest extends AbstractCmdTest {

    @Test
    void should_show_banner() {
        assertSuccessCli("?");
    }
    
    @Test
    void should_show_version() {
        assertSuccessCli("--version");
    }
    
    @Test
    void should_show_default_help() {
        assertSuccessCli("help");
    }
}
