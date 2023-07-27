package com.dtsx.astra.cli.test.utils;

import com.dtsx.astra.cli.utils.AstraCliUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * Utilities Test
 */
class UtilitiesTest {

    @Test
    void testCreateFolders() {
        AstraCliUtils.createHomeAstraFolders();
        Assertions.assertTrue(new File(AstraCliUtils.ASTRA_HOME
                + File.separator
                + AstraCliUtils.SCB_FOLDER).exists());

    }
}
