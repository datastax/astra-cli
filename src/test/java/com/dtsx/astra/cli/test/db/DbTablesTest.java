package com.dtsx.astra.cli.test.db;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.db.cdc.DbDeleteCdcCmd;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;

import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.client.core.query.Sort.descending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DbTablesTest extends AbstractCmdTest {

    static final String TEST_TABLE_SIMPLE = "test_table_simple";
    static final String TEST_TABLE_FULL = "test_table_full";

    @BeforeAll
    static void should_create_when_needed() {
        assertSuccessCli("db create %s --if-not-exists --vector".formatted(DB_TEST_VECTOR));

        // Initialize Astra DB JAVA
        Database db = new DataAPIClient(CliContext
                    .getInstance()
                    .getToken())
                .getDatabase(UUID.fromString(DaoDatabase.getInstance()
                    .getDatabase(DB_TEST_VECTOR)
                    .getId()));

        // Now Create a Table with simple fields
        Table<Row> tableSimple =db.createTable(TEST_TABLE_SIMPLE, new TableDefinition()
                .addColumnText("email")
                .addColumnInt("age")
                .addColumnText("name")
                .addColumnText("country")
                .addColumnBoolean("human")
                .partitionKey("email"), CreateTableOptions.IF_NOT_EXISTS);


        Table<Row> tableAllReturns = db.createTable(TEST_TABLE_FULL, new TableDefinition()
                        .addColumn("p_ascii", ColumnTypes.ASCII)
                        .addColumn("p_bigint", ColumnTypes.BIGINT)
                        .addColumn("p_blob", ColumnTypes.BLOB)
                        .addColumn("p_boolean", ColumnTypes.BOOLEAN)
                        .addColumn("p_date", ColumnTypes.DATE)
                        .addColumn("p_decimal", ColumnTypes.DECIMAL)
                        .addColumn("p_tinyint", ColumnTypes.TINYINT)
                        .addColumn("p_double", ColumnTypes.DOUBLE)
                        .addColumn("p_duration", ColumnTypes.DURATION)
                        .addColumn("p_duration2", ColumnTypes.DURATION)
                        .addColumn("p_float", ColumnTypes.FLOAT)
                        .addColumn("p_int", ColumnTypes.INT)
                        .addColumn("p_inet", ColumnTypes.INET)
                        .addColumn("p_smallint", ColumnTypes.SMALLINT)
                        .addColumn("p_text", ColumnTypes.TEXT)
                        .addColumn("p_text_nulled", ColumnTypes.TEXT)
                        .addColumn("p_text_omitted", ColumnTypes.TEXT)
                        .addColumn("p_time", ColumnTypes.TIME)
                        .addColumn("p_timestamp", ColumnTypes.TIMESTAMP)
                        .addColumn("p_tinyint", ColumnTypes.TINYINT)
                        .addColumn("p_uuid", ColumnTypes.UUID)
                        .addColumn("p_varint", ColumnTypes.VARINT)
                        .addColumnVector("p_vector", new ColumnDefinitionVector()
                                .dimension(3)
                                .metric(COSINE))
                        .addColumnList("p_list_int", ColumnTypes.INT)
                        .addColumnSet("p_set_int", ColumnTypes.INT)
                        .addColumnMap("p_map_text_text", ColumnTypes.TEXT, ColumnTypes.TEXT)
                        .addColumn("p_double_minf", ColumnTypes.DOUBLE)
                        .addColumn("p_double_pinf", ColumnTypes.DOUBLE)
                        .addColumn("p_float_nan", ColumnTypes.FLOAT)
                        .partitionKey("p_ascii", "p_bigint")
                        .clusteringColumns(ascending("p_int"), descending("p_boolean")),
                new CreateTableOptions().ifNotExists(true));
    }

    @Test
    @Order(1)
    public void shouldShowHelp() {
        assertSuccessCli("help db list-tables");
    }

    @Test
    @Order(2)
    public void shouldListTables() {
        // Should Create collection
        assertSuccessCli("db list-tables %s".formatted(DB_TEST_VECTOR));
    }

    @Test
    @Order(3)
    public void shouldTruncateTable() {
        // Should Create collection
        assertSuccessCli("db truncate-table %s --table %s".formatted(DB_TEST_VECTOR, TEST_TABLE_SIMPLE));
    }

    @Test
    @Order(4)
    public void shouldDescribeTable() {
        // Should Create collection
        assertSuccessCli("db describe-table %s --table %s".formatted(DB_TEST_VECTOR, TEST_TABLE_FULL));
    }

    @Test
    @Order(5)
    public void shouldDeleteTableIfExists() {
        // Should Create collection
        assertSuccessCli("db delete-table %s --table %s --if-exists".formatted(DB_TEST_VECTOR, TEST_TABLE_SIMPLE));
        assertSuccessCli("db delete-table %s --table %s --if-exists".formatted(DB_TEST_VECTOR, TEST_TABLE_SIMPLE));
    }

    @Test
    public void test() {
        CollectionDefinition cd;

    }

}
