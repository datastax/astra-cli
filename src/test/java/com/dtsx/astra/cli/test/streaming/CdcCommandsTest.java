package com.dtsx.astra.cli.test.streaming;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.streaming.cdc.StreamingCreateCdcCmd;
import com.dtsx.astra.cli.streaming.cdc.StreamingDeleteCdcCmd;
import com.dtsx.astra.cli.streaming.cdc.StreamingListCdcCmd;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test commands relative to CDC.
 */
public class CdcCommandsTest extends AbstractCmdTest {

   @Test
   public void shouldListCdc() {
       assertSuccessCli("streaming list-cdc clun-gcp-east1");
       assertSuccessCli("streaming list-cdc clun-gcp-east1 -o json");
       assertSuccessCli("streaming list-cdc clun-gcp-east1 -o csv");
   }

    @Test
    public void shouldListCdcFromDb() {
        assertSuccessCli("db list-cdc db2");
    }

    @Test
    public void shouldDeleteCdcDbInvalid() {
        assertExitCodeCli(ExitCode.NOT_FOUND, "db delete-cdc db2 -id invalid");
    }

    @Test
    public void shouldDeleteCdcDb() {
        assertSuccessCli( "db delete-cdc db2 -id ddcf6c81-table2");
    }

    @Test
    public void shouldDeleteCdcDb2() {
        assertSuccessCli( "db delete-cdc db2 -k ks2 --table users --tenant clun-gcp-east1 ");
    }

    @Test
    public void shouldCreateCdcDb() {
        assertSuccessCli( "db create-cdc db2 -k ks2 --table users --tenant clun-gcp-east1 ");
    }




}
