package com.datastax.astra.cli.test.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.db.OperationsDb;
import com.datastax.astra.cli.db.cqlsh.CqlShellUtils;
import com.datastax.astra.cli.db.dsbulk.DsBulkUtils;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DbCommandsTest extends AbstractCmdTest {
    
    static String DB_TEST = "astra_cli_test";
    
    @Test
    @Order(1)
    public void should_show_help() {
        assertSuccessCli("help");
        assertSuccessCli("help db");
        assertSuccessCli("help db create");
        assertSuccessCli("help db delete");
        assertSuccessCli("help db list");
        assertSuccessCli("help db cqlsh");
        assertSuccessCli("help db dsbulk");
        assertSuccessCli("help db resume");
        assertSuccessCli("help db status");
        assertSuccessCli("help db download-scb");
        assertSuccessCli("help db create-keyspace");
        assertSuccessCli("help db list-keyspaces");
    }
    
    @Test
    @Order(2)
    public void should_list_db() {
        assertSuccessCli("db list");
        assertSuccessCli("db list -v");
        assertSuccessCli("db list --no-color");
        assertSuccessCli("db list -o json");
        assertSuccessCli("db list -o csv");
    }
    
    @Test
    @Order(3)
    public void should_list_db_errors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "db list -w");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "db list DB");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "db list -o yaml");
    }
    
    @Test
    @Order(3)
    public void should_create_db() throws DatabaseNameNotUniqueException {
        // When
        assertSuccessCli("db create %s --if-not-exist --wait".formatted(DB_TEST));
        // Then
        Assertions.assertTrue(  OperationsDb.getDatabaseClient(DB_TEST).isPresent());
        // Database is pending
        assertSuccessCli("db status %s".formatted(DB_TEST));
        
    }
    
   
    
    /*
    @Test
    public void should_create_dot_env()  throws Exception {
        assertSuccessCli("db create-dotenv mtg");
    }
    */
    
    @Test
    public void should_install_DsBulk()  throws Exception {
        DsBulkUtils.installDsBulk();
        Assertions.assertTrue(DsBulkUtils.isDsBulkInstalled());
    }
}
