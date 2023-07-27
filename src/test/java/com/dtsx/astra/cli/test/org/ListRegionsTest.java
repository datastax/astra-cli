package com.dtsx.astra.cli.test.org;

import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.Test;

/**
 * Operation on org.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
class ListRegionsTest extends AbstractCmdTest {

    @Test
    void tesListDbRegionsServerless()  {
        assertSuccessCli("db list-regions-serverless");
        assertSuccessCli("db list-regions-serverless -o csv");
        assertSuccessCli("db list-regions-serverless -o json");
        assertSuccessCli("db list-regions-serverless --cloud aws");
        assertSuccessCli("db list-regions-serverless --cloud aws --filter eu");
    }

    @Test
    void testListDbRegionsClassic() {
        assertSuccessCli("db list-regions-classic");
        assertSuccessCli("db list-regions-classic --cloud aws");
        assertSuccessCli("db list-regions-classic --cloud aws --filter us");
        assertSuccessCli("db list-regions-classic --cloud aws --filter Mumbai -o json");
        assertSuccessCli("db list-regions-classic -o json");
        assertSuccessCli("db list-regions-classic -o csv");
    }

    @Test
    void testListDbRegionsStreaming() {
        assertSuccessCli("streaming list-regions");
    }
}
