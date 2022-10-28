package com.dtsx.astra.cli.test;

import org.junit.jupiter.api.Test;

/**
 * Test Main behaviour.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class MainCmdTest extends AbstractCmdTest {

    @Test
    public void should_show_banner() {
        assertSuccessCli("?");
    }
    
    @Test
    public void should_show_version() {
        assertSuccessCli("--version");
    }
    
    @Test
    public void should_show_default_help() {
        assertSuccessCli("help");
    }
}
