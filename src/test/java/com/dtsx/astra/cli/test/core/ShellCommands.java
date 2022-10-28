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
public class ShellCommands extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void showVersion() throws Exception {
        assertSuccessCli("--version");
    }
    
    
}
