package com.dtsx.astra.cli.test.org;

import org.junit.jupiter.api.Test;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Operation on org.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
class OrgCommandTest extends AbstractCmdTest {
    
    @Test
    void should_display_org() {
        assertSuccessCli("org");
        assertSuccessCli("org -o json");
        assertSuccessCli("org -o csv");
        assertSuccessCli("org id");
        assertSuccessCli("org name");
    }
    
    @Test
    void testThrowErrors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "organ");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "org invalid");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "org -o yaml");
    }

}
