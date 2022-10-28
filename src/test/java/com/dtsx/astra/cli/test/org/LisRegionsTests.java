package com.dtsx.astra.cli.test.org;

import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.Test;

/**
 * Operation on org.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class LisRegionsTests extends AbstractCmdTest {

    @Test
    public void tesListDbRegionsServerless()  {
        //assertSuccessCli("org list-regions-db-serverless");
        //assertSuccessCli("org list-regions-db-serverless -o csv");
        //assertSuccessCli("org list-regions-db-serverless -o json");
        //assertSuccessCli("org list-regions-db-serverless --cloud aws");
        assertSuccessCli("org list-regions-db-serverless --cloud aws --filter eu");
    }

    @Test
    public void testListDbRegionsClassic() {
        assertSuccessCli("org list-regions-db-classic");
        assertSuccessCli("org list-regions-db-classic --cloud aws");
        assertSuccessCli("org list-regions-db-classic --cloud aws --filter us");
        assertSuccessCli("org list-regions-db-classic --cloud aws --filter Mumbai -o json");
        assertSuccessCli("org list-regions-db-classic -o json");
        assertSuccessCli("org list-regions-db-classic -o csv");
    }

    @Test
    public void testListDbRegionsStreaming() {
        assertSuccessCli("streaming list-regions");
    }
}
