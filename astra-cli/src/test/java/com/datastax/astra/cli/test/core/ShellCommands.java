package com.datastax.astra.cli.test.core;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Interactive commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class ShellCommands extends AbstractCmdTest {
    
    @Test
    @Order(1)
    public void startInteractive() throws Exception {
        //InputStream in = new ByteArrayInputStream("exit\n".getBytes());
        //System.setIn(in);
        //AstraCli.runCli(AstraCli.class, new String[] {});
    }
    
    @Test
    @Order(2)
    public void showVersion() throws Exception {
        astraCli("--version");
    }
}
