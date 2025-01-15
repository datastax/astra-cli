package com.dtsx.astra.cli.db.table;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionList;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionMap;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionSet;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.dtsx.astra.cli.core.out.AstraAnsiColors;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.core.out.StringBuilderAnsi;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.db.exception.TableNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceTables {

    /**
     * column names.
     */
    static final String COLUMN_NAME = "Name";

    /**
     * column names.
     */
    static final String COLUMN_METRIC = "Similarity Metric ";

    /**
     * column names.
     */
    static final String COLUMN_PARTITION_KEY = "Partition Key";

    /**
     * column names.
     */
    static final String COLUMN_CLUSTERING_COLUMNS = "Clustering Columns";

    /**
     * column names.
     */
    static final String OTHERS_COLUMNS = "OTHERS COLUMNS";

    /**
     * column names.
     */
    static final String COLUMN_COUNT = "Estim. Count";

    /**
     * Singleton Pattern
     */
    private static ServiceTables instance;

    /**
     * Access to databases object.
     */
    private final DaoDatabase dbDao;

    /**
     * Singleton Pattern.
     *
     * @return instance of the service.
     */
    public static synchronized ServiceTables getInstance() {
        if (null == instance) {
            instance = new ServiceTables();
        }
        return instance;
    }

    /**
     * Default Constructor.
     */
    private ServiceTables() {
        this.dbDao = DaoDatabase.getInstance();
    }

    /**
     * List keyspaces of a database.
     *
     * @param databaseName database name
     */
    public void listTables(String databaseName, String keyspace) {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME, 20);
        dbDao.getDataAPIDatabase(databaseName, keyspace).listTables().forEach(col -> {
            Table<Row> table = dbDao
                            .getDataAPIDatabase(databaseName, keyspace)
                            .getTable(col.getName());
            TableDefinition def = col.getDefinition();
            Map<String, String> rf = new HashMap<>();
            rf.put(COLUMN_NAME,  table.getName());
            sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }

    public void describeTable(String databaseName, String keyspace, String tableName) {
        Table<Row> myTable = dbDao
                .getDataAPIDatabase(databaseName, keyspace)
                .getTable(tableName);
        TableDefinition info = myTable.getDefinition();
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow(COLUMN_NAME, tableName);
        sht.addPropertyRow("", "");
        sht.addPropertyRow(StringBuilderAnsi.colored("COLUMNS", AstraAnsiColors.YELLOW_300), "");
        info.getColumns().entrySet().forEach(entry -> {
            String value = entry.getValue().getType().name();
            if (ColumnTypes.LIST.equals(entry.getValue().getType()) ||
                ColumnTypes.SET.equals(entry.getValue().getType()) ||
                ColumnTypes.VECTOR.equals(entry.getValue().getType()) ||
                ColumnTypes.MAP.equals(entry.getValue().getType())) {
                if (entry.getValue().getApiSupport() != null) {
                    value = entry.getValue().getApiSupport().getCqlDefinition().toUpperCase();
                } else {
                    value = entry.getValue().getType() + "<?>";
                }
            }
            sht.addPropertyRow(entry.getKey(), value);
        });
        sht.addPropertyRow("", "");
        sht.addPropertyRow(StringBuilderAnsi.colored("PRIMARY KEY", AstraAnsiColors.YELLOW_300), "");
        sht.addPropertyListRows("Partition Key", info.getPrimaryKey().getPartitionBy());
        sht.addPropertyListRows("Clustering Columns", info.getPrimaryKey()
                .getPartitionSort().entrySet()
                .stream()
                .map(entry -> entry.getKey()
                        + "(" + (entry.getValue() == 1 ? "ASC" : "DESC") + ")")
                .collect(Collectors.toList()));
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Delete a collection.
     *
     * @param databaseName
     *      database name
     * @param keyspace
     *      name of the keyspace to target this collection
     * @param tableName
     *      collection name
     * @param ifExist
     *      will delete only if exists and does not give error
     */
    public void deleteTable(String databaseName, String keyspace, String tableName, boolean ifExist) {
        Database db = dbDao.getDataAPIDatabase(databaseName, keyspace);
        // Expected collection is not there
        if (!db.tableExists(tableName)) {
            if (ifExist) {
                LoggerShell.info("Table '%s' does not exist in '%s'.".formatted(tableName, databaseName));
            } else {
                throw new TableNotFoundException(databaseName, tableName);
            }
        } else {
            dbDao.getDataAPIDatabase(databaseName, keyspace).dropTable(tableName);
            LoggerShell.success("Table '%s' as been deleted from '%s'.".formatted(tableName, databaseName));
        }
    }

    public void truncateTable(String databaseName, String keyspace, String tableName) {
        Database db = dbDao.getDataAPIDatabase(databaseName, keyspace);
        // Expected collection is not there
        if (!db.tableExists(tableName)) {
            throw new TableNotFoundException(databaseName, tableName);
        } else {
            dbDao.getDataAPIDatabase(databaseName, keyspace).getTable(tableName).deleteAll();
            LoggerShell.success("Table '%s' as been truncated from '%s'.".formatted(tableName, databaseName));
        }
    }

    /**
     * Test table existence.
     *
     * @param databaseName
     *      database name
     * @param keyspace
     *      name of the keyspace to target this collection
     * @param tableName
     *      collection name
     */
    public boolean isTableExists(String databaseName, String keyspace, String tableName) {
        return dbDao.getDataAPIDatabase(databaseName, keyspace).tableExists(tableName);
    }


}
