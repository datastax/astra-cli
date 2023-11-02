package com.dtsx.astra.cli.db.collection;

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

import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.sdk.db.domain.Database;
import io.stargate.sdk.json.domain.CollectionDefinition;
import io.stargate.sdk.json.domain.SimilarityMetric;

import java.util.HashMap;
import java.util.Map;

public class ServiceCollection {

    /**
     * column names.
     */
    static final String COLUMN_NAME = "Name";

    /**
     * column names.
     */
    static final String COLUMN_METRIC = "Metric";

    /**
     * column names.
     */
    static final String COLUMN_DIMENSION = "Dimension";

    /**
     * Singleton Pattern
     */
    private static ServiceCollection instance;

    /**
     * Access to databases object.
     */
    private final DaoDatabase dbDao;

    /**
     * Singleton Pattern.
     *
     * @return instance of the service.
     */
    public static synchronized ServiceCollection getInstance() {
        if (null == instance) {
            instance = new ServiceCollection();
        }
        return instance;
    }

    /**
     * Default Constructor.
     */
    private ServiceCollection() {
        this.dbDao = DaoDatabase.getInstance();
    }

    /**
     * List keyspaces of a database.
     *
     * @param databaseName database name
     */
    public void listCollections(String databaseName) {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME, 20);
        sht.addColumn(COLUMN_DIMENSION, 10);
        sht.addColumn(COLUMN_METRIC, 10);
        dbDao.getAstraDB(databaseName).findAllCollections().forEach(col -> {
            Map<String, String> rf = new HashMap<>();
            rf.put(COLUMN_NAME,      col.getName());
            if (col.getOptions() == null || col.getOptions().getVector() == null) {
                rf.put(COLUMN_DIMENSION, "");
                rf.put(COLUMN_METRIC,    "");
            } else {
                rf.put(COLUMN_DIMENSION, String.valueOf(col.getOptions().getVector().getDimension()));
                rf.put(COLUMN_METRIC, col.getOptions().getVector().getMetric().name());
            }
            sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Create collection based on arguments.
     * @param databaseName
     *      current database name
     * @param cco
     *      current collection creation arguments
     */
    public void createCollection(String databaseName, CreateCollectionOption cco) {
        CollectionDefinition.Builder builder = CollectionDefinition.builder().name(cco.collection());
        if (cco.dimension() != null) {
            builder.vectorDimension(cco.dimension());
            builder.similarityMetric(cco.metric());
        }
        dbDao.getAstraDB(databaseName).createCollection(builder.build());
    }

    /**
     * Delete a collection.
     *
     * @param databaseName
     *      database name
     * @param collectionName
     *      collection name
     */
    public void deleteCollection(String databaseName, String collectionName) {
        dbDao.getAstraDB(databaseName).deleteCollection(collectionName);
    }

    /**
     * Delete a collection.
     *
     * @param databaseName
     *      database name
     * @param collectionName
     *      collection name
     */
    public boolean isCollectionExists(String databaseName, String collectionName) {
        return dbDao.getAstraDB(databaseName).isCollectionExists(collectionName);
    }
}
