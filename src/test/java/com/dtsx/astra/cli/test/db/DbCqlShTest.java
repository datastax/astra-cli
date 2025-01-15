package com.dtsx.astra.cli.test.db;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.dtsx.astra.cli.db.cqlsh.ServiceCqlShell;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.dtsx.astra.cli.utils.FileUtils;

/**
 * Test on cqlsh.
 */
class DbCqlShTest extends AbstractCmdTest {

    final static String TABLE_TEST = "cities_by_country";

    @BeforeAll
    static void initForCqlsh() {
        assertSuccessCli("db create %s -k %s --if-not-exist".formatted(DB_TEST, KEYSPACE_TEST));
        assertSuccessCli("db cqlsh %s -f src/test/resources/cdc_dataset.cql".formatted(DB_TEST));
    }

    @Test
    @Order(1)
    @DisplayName("Installing cqlsh")
    void testShouldInstallCqlSh() {
        if (!disableTools) {
            File cqlshFolder = new File(AstraCliUtils.ASTRA_HOME + File.separator + "cqlsh-astra");
            FileUtils.deleteDirectory(cqlshFolder);
            // install
            ServiceCqlShell.getInstance().install();
            Assertions.assertTrue(ServiceCqlShell.getInstance().isInstalled());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Execute command")
    void testShouldExecute() {
        assertSuccessCql(DB_TEST, "select * from default_keyspace.demo LIMIT 20;");
    }
}
