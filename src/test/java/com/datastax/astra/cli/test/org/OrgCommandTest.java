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
    public void should_display_org() {
        assertSuccessCli("org");
        assertSuccessCli("org -o json");
        assertSuccessCli("org -o csv");
        assertSuccessCli("org id");
        assertSuccessCli("org name");
    }

    @Test
    public void tesListDbRegionsServerless()  {
        assertSuccessCli("org list-regions-db-serverless");
        assertSuccessCli("org list-regions-db-serverless -o csv");
        assertSuccessCli("org list-regions-db-serverless -o json");
    }

    @Test
    public void testListDbRegionsClassic() {
        assertSuccessCli("org list-regions-db-classic");

        assertSuccessCli("org list-regions-db-classic --cloud aws");

        assertSuccessCli("org list-regions-db-classic --cloud aws --filter us");
        //assertSuccessCli("org list-regions-db-classic -o json");
        //assertSuccessCli("org list-regions-db-classic -o csv");
    }
    
    @Test
    public void testThrowErrors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "organ");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "org invalid");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "org -o yaml");
    }

}
