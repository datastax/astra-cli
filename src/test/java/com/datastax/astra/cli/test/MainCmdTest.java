package com.datastax.astra.cli.test;

import org.junit.jupiter.api.Test;

/**
 * Test Main behaviour.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class MainCmdTest extends AbstractCmdTest {

    @Test
    public void should_show_banner() {
        assertSuccessCli("version");
    }
    
    @Test
    public void should_show_version() {
        assertSuccessCli("--version");
    }
}
