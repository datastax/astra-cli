package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test Collections
 */
public class DbCollectionsTest extends AbstractCmdTest {

    static final String TEST_COLLECTION_SIMPLE = "test_collection_simple";
    static final String TEST_COLLECTION_VECTOR = "test_collection_vector";

    @BeforeAll
    static void should_create_when_needed() {
        //assertSuccessCli("db create %s --if-not-exist".formatted(DB_TEST));
        //assertSuccessCli("db create %s --if-not-exist --vector".formatted(DB_TEST_VECTOR));
    }

    @Test
    public void shouldCleanCollections() {
        assertSuccessCli("db delete-collection %s --collection %s".formatted(DB_TEST_VECTOR, TEST_COLLECTION_SIMPLE));
        assertSuccessCli("db delete-collection %s --collection %s".formatted(DB_TEST_VECTOR, TEST_COLLECTION_VECTOR));
    }

    @Test
    public void shouldCreateCollectionSimple() {
        // Should Create collection
        assertSuccessCli("db create-collection %s --collection %s".formatted(DB_TEST_VECTOR, TEST_COLLECTION_SIMPLE));
    }

    @Test
    public void shouldCreateCollectionVector() {
        assertSuccessCli("db create-collection  %s --collection %s --metric cosine --dimension 1536"
                .formatted(DB_TEST_VECTOR, TEST_COLLECTION_VECTOR));
    }

    @Test
    public void shouldCreateCollectionFull() {
        assertSuccessCli(("db create-collection  %s " +
                "--collection %s " +
                "--metric cosine " +
                "--dimension 1536 " +
                "--indexing-allow field1,field2 " +
                "--default-id uuid " +
                "--embedding-provider openai " +
                "--embedding-model text-embedding-ada-002")
                .formatted(DB_TEST_VECTOR, "colopen"));
    }

    @Test
    public void shouldListCollections() {
        assertSuccessCli("db list-collections %s".formatted(DB_TEST_VECTOR));
    }

    @Test
    public void shouldRaiseError() {
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, ("db create-collection %s " +
                "--collection test --metric CHAUSSETTE").formatted(DB_TEST_VECTOR));
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, ("db create-collection %s " +
                "--collection test --dimension -2").formatted(DB_TEST_VECTOR));

    }


}
