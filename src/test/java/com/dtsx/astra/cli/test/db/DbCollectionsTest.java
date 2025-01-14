package com.dtsx.astra.cli.test.db;

import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.internal.api.DataAPIStatus;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test Collections
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DbCollectionsTest extends AbstractCmdTest {

    static final String TEST_COLLECTION_SIMPLE = "test_collection_simple";
    static final String TEST_COLLECTION_VECTOR = "test_collection_vector";

    @BeforeAll
    static void should_create_when_needed() {
        assertSuccessCli("db create %s --if-not-exists --vector".formatted(DB_TEST_VECTOR));

        // Empty all collections for our test
        assertSuccessCli("db delete-collection %s --collection %s --if-exists"
                .formatted(DB_TEST_VECTOR, TEST_COLLECTION_SIMPLE));

        assertExitCodeCli(ExitCode.NOT_FOUND,
                "db delete-collection %s --collection %s"
                        .formatted(DB_TEST_VECTOR, TEST_COLLECTION_SIMPLE));
    }

    @Test
    @Order(1)
    public void shouldShowHelp() {
        assertSuccessCli("help");
        assertSuccessCli("help db");
        assertSuccessCli("help db delete-collection");
        assertSuccessCli("help db create-collection");
        assertSuccessCli("help db list-collections");
    }

    @Test
    @Order(2)
    public void shouldCreateCollectionSimple() {
        // Should Create collection
        assertSuccessCli("db create-collection %s --collection %s --if-not-exists"
                .formatted(DB_TEST_VECTOR, TEST_COLLECTION_SIMPLE));

        // Should return an error as it already exists now
        assertExitCodeCli(ExitCode.ALREADY_EXIST, "db create-collection %s --collection %s"
                .formatted(DB_TEST_VECTOR, TEST_COLLECTION_SIMPLE));
    }

    @Test
    @Order(3)
    public void shouldCreateCollectionVector() {
        assertSuccessCli(("db create-collection %s " +
                "--collection %s " +
                "--metric cosine " +
                "--dimension 1536 " +
                "--if-not-exists")
                .formatted(DB_TEST_VECTOR, TEST_COLLECTION_VECTOR));
    }

    @Test
    @Order(4)
    public void shouldListCollections() {
        DataAPIStatus s;
        DataAPIResponse rew;
        DataAPIResponse status;
        CollectionDeserializer ss;
        assertSuccessCli("db list-collections %s ".formatted(DB_TEST_VECTOR));
    }

    @Test
    @Order(5)
    public void shouldDescribeExistingCollection() {
        assertSuccessCli(("db describe-collection %s " +
                "--collection %s ").formatted(DB_TEST_VECTOR, TEST_COLLECTION_VECTOR));
    }


    @Test
    @Order(6)
    public void shouldCreateCollectionFull() {
        assertSuccessCli(("db create-collection  %s " +
                "--collection %s " +
                "--metric dot_product " +
                "--dimension 1024 " +
                "--indexing-allow field1,field2 " +
                "--default-id uuid " +
                "--embedding-provider nvidia " +
                "--embedding-model NV-Embed-QA " +
                "--if-not-exists")
                //"--embedding-key OPENAI_API_KEY")
                .formatted(DB_TEST_VECTOR, "nvidia_clun"));
    }

    @Test
    @Order(7)
    public void shouldRaiseError() {
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, ("db create-collection %s " +
                "--collection test --metric CHAUSSETTE").formatted(DB_TEST_VECTOR));
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, ("db create-collection %s " +
                "--collection test --dimension -2").formatted(DB_TEST_VECTOR));
    }

    @Test
    @Order(8)
    public void shouldCleanCollections() {
        assertSuccessCli("db delete-collection %s --collection %s".formatted(DB_TEST_VECTOR, TEST_COLLECTION_SIMPLE));
        assertSuccessCli("db delete-collection %s --collection %s".formatted(DB_TEST_VECTOR, TEST_COLLECTION_VECTOR));
    }

    @Test
    @Order(9)
    public void shouldFindEmbeddingProvider() {
        assertSuccessCli("db list-embedding-providers %s".formatted(DB_TEST_VECTOR));
    }

    @Test
    @Order(9)
    public void shouldDescriptionEmbeddingProvider() {
        //assertExitCodeCli(ExitCode.INVALID_ARGUMENT,
        //        ("db describe-embedding-provider %s --embedding-provider %s"
        //                .formatted(DB_TEST_VECTOR, "invalid")));

        assertSuccessCli("help db describe-embedding-provider");

        assertSuccessCli("db describe-embedding-provider %s --embedding-provider %s"
                .formatted(DB_TEST_VECTOR, "nvidia"));
    }

}
