package com.dtsx.astra.cli.test.streaming;

import com.dtsx.astra.cli.streaming.StreamingListCmd;
import com.dtsx.astra.cli.streaming.cdc.StreamingCreateCdcCmd;
import com.dtsx.astra.cli.streaming.cdc.StreamingDeleteCdcCmd;
import com.dtsx.astra.cli.streaming.cdc.StreamingGetCdcCmd;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test commands relative to CDC.
 */
public class CdcCommandsTest extends AbstractCmdTest {

    @Test
    public void testCdc() {
        // waiting for test cdc functions
        Assertions.assertThrows(UnsupportedOperationException.class, () ->  new StreamingCreateCdcCmd().execute());
        Assertions.assertThrows(UnsupportedOperationException.class, () ->  new StreamingDeleteCdcCmd().execute());
        Assertions.assertThrows(UnsupportedOperationException.class, () ->  new StreamingGetCdcCmd().execute());
    }

}
