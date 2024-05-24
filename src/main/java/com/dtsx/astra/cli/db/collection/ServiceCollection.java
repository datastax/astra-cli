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

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionIdTypes;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.SimilarityMetric;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.db.DaoDatabase;

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
     * column names.
     */
    static final String COLUMN_SERVICE = "AI Provider";

    /**
     * column names.
     */
    static final String COLUMN_MODEL = "AI Model";

    /**
     * column names.
     */
    static final String COLUMN_UUID = "id type";


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
    public void listCollections(String databaseName, String keyspace) {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME, 20);
        sht.addColumn(COLUMN_UUID, 10);
        sht.addColumn(COLUMN_DIMENSION, 10);
        sht.addColumn(COLUMN_METRIC, 10);
        sht.addColumn(COLUMN_SERVICE, 10);
        sht.addColumn(COLUMN_MODEL, 20);

        dbDao.getDataAPIDatabase(databaseName, keyspace).listCollections().forEach(col -> {
            Map<String, String> rf = new HashMap<>();
            rf.put(COLUMN_NAME,      col.getName());
            rf.put(COLUMN_DIMENSION, "");
            rf.put(COLUMN_METRIC,    "");
            rf.put(COLUMN_SERVICE,   "");
            rf.put(COLUMN_MODEL,     "");
            rf.put(COLUMN_UUID,      "default");

            if (col.getOptions() != null && col.getOptions().getVector() != null) {
              CollectionOptions.VectorOptions vOptions =  col.getOptions().getVector();
              rf.put(COLUMN_DIMENSION, String.valueOf(vOptions.getDimension()));
              rf.put(COLUMN_METRIC, vOptions.getMetric());
              if (vOptions.getService() != null) {
                  rf.put(COLUMN_SERVICE, vOptions.getService().getProvider());
                  rf.put(COLUMN_MODEL, vOptions.getService().getModelName());
              }
            }
            if (col.getOptions().getIndexing() != null) {
                System.out.println("Allow: " + col.getOptions().getIndexing().getAllow());
                System.out.println("Deny: " + col.getOptions().getIndexing().getDeny());
            }
            if (col.getOptions().getDefaultId() != null) {
                rf.put(COLUMN_UUID, col.getOptions().getDefaultId().getType());
            }

            sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Create collection based on arguments.
     * @param databaseName
     *      current database name
     * @param keyspace
     *      name of the keyspace to target this collection
     * @param cco
     *      current collection creation arguments
     */
    public Collection<Document> createCollection(String databaseName, String keyspace, CreateCollectionOption cco) {
        Database db = dbDao.getDataAPIDatabase(databaseName, keyspace);
        CollectionOptions.CollectionOptionsBuilder options = CollectionOptions.builder();

        // Parameters Validations
        if (cco.indexingAllow() != null && cco.indexingAllow().length > 0) {
            options.indexingAllow(cco.indexingAllow());
        } else if (cco.indexingDeny() != null && cco.indexingDeny().length > 0) {
            options.indexingDeny(cco.indexingDeny());
        }

        // Vectorize
        if (cco.embeddingModel() != null) {
            options.vectorize(cco.embeddingProvider(), cco.embeddingModel());
        }

        if (cco.defaultId() != null) {
            options.defaultIdType(cco.defaultId());
        }

        // Dimension
        if (cco.dimension() != null) {
            options.vectorDimension(cco.dimension());
        }

        // Metrics
        if (cco.metric() != null) {
            options.vectorSimilarity(cco.metric());
        }
        return db.createCollection(cco.name(), options.build());
    }

    /**
     * Delete a collection.
     *
     * @param databaseName
     *      database name
     * @param keyspace
     *      name of the keyspace to target this collection
     * @param collectionName
     *      collection name
     */
    public void deleteCollection(String databaseName, String keyspace, String collectionName) {
        dbDao.getDataAPIDatabase(databaseName, keyspace).dropCollection(collectionName);
    }

    /**
     * Delete a collection.
     *
     * @param databaseName
     *      database name
     * @param keyspace
     *      name of the keyspace to target this collection
     * @param collectionName
     *      collection name
     */
    public boolean isCollectionExists(String databaseName,  String keyspace, String collectionName) {
        return dbDao.getDataAPIDatabase(databaseName, keyspace).collectionExists(collectionName);
    }
}
