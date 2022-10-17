package com.datastax.astra.cli.test.db;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.db.DatabaseDao;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Test command to list configurations.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
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
        Assertions.assertTrue(DatabaseDao.getInstance().getDatabaseClient(DB_TEST).isPresent());
        // Database is pending
        assertSuccessCli("db status %s".formatted(DB_TEST));
    }
    
    @Test
    @Order(4)
    public void should_get_db() throws DatabaseNameNotUniqueException {
        assertSuccessCli("db get %s".formatted(DB_TEST));
        assertSuccessCli("db get %s -o json".formatted(DB_TEST));
        assertSuccessCli("db get %s -o csv".formatted(DB_TEST));
        assertSuccessCli("db get %s --key id".formatted(DB_TEST));
        assertSuccessCli("db get %s --key status".formatted(DB_TEST));
        assertSuccessCli("db get %s --key cloud".formatted(DB_TEST));
        assertSuccessCli("db get %s --key keyspace".formatted(DB_TEST));
        assertSuccessCli("db get %s --key keyspaces".formatted(DB_TEST));
        assertSuccessCli("db get %s --key region".formatted(DB_TEST));
        assertSuccessCli("db get %s --key regions".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db get %s --invalid".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db get does-not-exist");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "db get %s -o yaml".formatted(DB_TEST));
    }
    
    @Test
    @Order(5)
    public void should_list_keyspaces()  {
        assertSuccessCli("db list-keyspaces %s".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s -v".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s --no-color".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s -o json".formatted(DB_TEST));
        assertSuccessCli("db list-keyspaces %s -o csv".formatted(DB_TEST));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db list-keyspaces does-not-exist");
    }
    
    @Test
    @Order(6)
    public void should_create_keyspaces()  {
        String randomKS = "ks_" + UUID
                .randomUUID().toString()
                .replaceAll("-", "").substring(0, 8);
        assertSuccessCli("db create-keyspace %s -k %s -v".formatted(DB_TEST, randomKS));
        assertExitCodeCli(ExitCode.NOT_FOUND, 
                "db create-keyspace %s -k %s -v".formatted("does-not-exist", randomKS));
    }
    
    @Test
    @Order(7)
    public void testShould_download_scb()  {
        assertSuccessCli("db download-scb %s -f %s".formatted(DB_TEST, "/tmp/sample.zip"));
        assertExitCodeCli(ExitCode.NOT_FOUND, "db download-scb %s".formatted("invalid"));
    }
    
    @Test
    @Order(8)
    public void should_resumedb()  {
        assertExitCodeCli(ExitCode.NOT_FOUND, "db resume %s".formatted("invalid"));
    }
   
}
