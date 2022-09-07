package com.datastax.astra;

import org.junit.jupiter.api.Test;

/**
 * Interactive commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class ShellCommands extends AbstractAstraCliTest {

    @Test
    public void startInteractive() throws Exception {
        astraCli();
    }
    
    @Test
    public void showVersion() throws Exception {
        astraCli("--version");
    }
}
