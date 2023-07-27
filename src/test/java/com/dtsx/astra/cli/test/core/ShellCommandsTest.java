package com.dtsx.astra.cli.test.core;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Interactive commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
class ShellCommandsTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    void showVersion() {
        assertSuccessCli("--version");
    }

    @Test
    @Order(2)
    void showBanner() {
        assertSuccessCli("?");
    }
    
}
