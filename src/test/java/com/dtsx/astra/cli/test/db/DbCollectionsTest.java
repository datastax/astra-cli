package com.dtsx.astra.cli.test.db;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.Test;

/**
 * Test Collections
 */
public class DbCollectionsTest extends AbstractCmdTest {

    static final String TEST_COLLECTION_SIMPLE = "test_collection_simple";
    static final String TEST_COLLECTION_VECTOR = "test_collection_vector";

    @Test
    public void shouldCleanCollections() {
        // Should Delete Collections
        assertSuccessCli("db delete-collection %s --collection %s".formatted(DB_TEST_VECTOR, TEST_COLLECTION_SIMPLE));
        assertSuccessCli("db delete-collection %s --collection %s".formatted(DB_TEST_VECTOR, TEST_COLLECTION_VECTOR));
    }

    @Test
    public void shouldCreateCollection() {
        // Should Create collection
        assertSuccessCli("db create-collection %s --collection %s".formatted(DB_TEST_VECTOR, TEST_COLLECTION_SIMPLE));
        assertSuccessCli("db create-collection  %s --collection %s --metric cosine --dimension 1536"
               .formatted(DB_TEST_VECTOR, TEST_COLLECTION_VECTOR));
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
