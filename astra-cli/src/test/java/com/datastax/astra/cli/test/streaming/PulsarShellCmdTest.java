package com.datastax.astra.cli.test.streaming;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.streaming.pulsarshell.PulsarShellUtils;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Command on Pulsar Shell.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class PulsarShellCmdTest extends AbstractCmdTest {
    
    @Test
    public void should_install_pussarShell()  throws Exception {
        PulsarShellUtils.installPulsarShell();
        Assertions.assertTrue(PulsarShellUtils.isPulsarShellInstalled());
    }
    
    @Test
    public void should_puslar_shell() {
        astraCli("streaming", "pulsar-shell", "trollsquad-2022");
    }
    
    
}
