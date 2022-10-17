package com.datastax.astra.cli.test.org;

import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Operation on org.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class OrgCommandTest extends AbstractCmdTest {
    
    @Test
    public void should_display_org()  throws Exception {
        assertSuccessCli("org");
        assertSuccessCli("org -o json");
        assertSuccessCli("org -o csv");
        assertSuccessCli("org id");
        assertSuccessCli("org name");
        assertSuccessCli("org list-regions-classic");
        assertSuccessCli("org list-regions-classic -o json");
        assertSuccessCli("org list-regions-classic -o csv");
        assertSuccessCli("org list-regions-serverless");
        assertSuccessCli("org list-regions-serverless -o csv");
        assertSuccessCli("org list-regions-serverless -o json");
    }
    
    @Test
    public void should_org_errors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "organe");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "org invalid");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "org -o yaml");
    }

}
