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
import com.datastax.astra.client.model.CollectionInfo;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.utils.AnsiUtils;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.JsonOutput;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.db.DaoDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceCollection {

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
    static final String COLUMN_DIMENSION = "Vector Dimension ";

    /**
     * column names.
     */
    static final String COLUMN_SERVICE = "AI Provider ";

    /**
     * column names.
     */
    static final String COLUMN_MODEL = "AI Model ";

    /**
     * column names.
     */
    static final String COLUMN_UUID = "DefaultId";

    /**
     * column names.
     */
    static final String COLUMN_DISPLAY_NAME = "DisplayName";

    /**
     * column names.
     */
    static final String COLUMN_MODELS = "Models";

    /**
     * column names.
     */
    static final String COLUMN_PARAMETERS = "Parameters";

    /**
     * column names.
     */
    static final String COLUMN_AUTH_HEADER = "Au.Head";

    /**
     * column names.
     */
    static final String COLUMN_AUTHSECRET = "S.Secret";


    /**
     * column names.
     */
    static final String COLUMN_URL = "URL";

    /**
     * column names.
     */
    static final String COLUMN_KEY = "Key";

    /**
     * column names.
     */
    static final String COLUMN_ALLOWS = "indexingAllows";

    /**
     * column names.
     */
    static final String COLUMN_DENIES = "indexingDenies";

    /**
     * column names.
     */
    static final String COLUMN_AUTHENTICATION = "Authentication";

    /**
     * column names.
     */
    static final String COLUMN_COUNT = "Estim. Count";

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
    public void listEmbeddingProviders(String databaseName) {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_KEY, 10);
        sht.addColumn(COLUMN_DISPLAY_NAME, 20);
        sht.addColumn(COLUMN_MODELS, 3);
        sht.addColumn(COLUMN_PARAMETERS, 3);
        sht.addColumn(COLUMN_AUTH_HEADER, 3);
        sht.addColumn(COLUMN_AUTHSECRET, 3);

        Map<String, EmbeddingProvider> myCollec = dbDao
                .getDataAPIDatabase(databaseName)
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders();
        for (Map.Entry<String, EmbeddingProvider> entry : myCollec.entrySet()) {
            EmbeddingProvider vOptions = entry.getValue();
            Map<String, String> rf = new HashMap<>();
            rf.put(COLUMN_KEY, entry.getKey());
            rf.put(COLUMN_DISPLAY_NAME, "N/A");
            if (entry.getValue().getDisplayName() != null) {
                rf.put(COLUMN_DISPLAY_NAME, entry.getValue().getDisplayName());
            }
            rf.put(COLUMN_MODELS, String.valueOf(entry.getValue().getModels().size()));
            rf.put(COLUMN_PARAMETERS, String.valueOf(entry.getValue().getParameters().size()));
            rf.put(COLUMN_AUTH_HEADER, "");
            if (entry.getValue().getHeaderAuthentication().isPresent()) {
                rf.put(COLUMN_AUTH_HEADER, "■");
            }
            rf.put(COLUMN_AUTHSECRET, "");
            entry.getValue().getSharedSecretAuthentication().ifPresent(s -> {
                rf.put(COLUMN_AUTHSECRET, "■");
            });
            sht.getCellValues().add(rf);
        }
        AstraCliConsole.printShellTable(sht);
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
            if (col.getOptions().getDefaultId() != null) {
                rf.put(COLUMN_UUID, col.getOptions().getDefaultId().getType());
            }
            sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }

    public void describeCollection(String databaseName, String keyspace, String collection) {
        Collection<Document> myCollec = dbDao
                .getDataAPIDatabase(databaseName, keyspace)
                .getCollection(collection);
        CollectionInfo info = myCollec.getDefinition();
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow(COLUMN_NAME, info.getName());
        sht.addPropertyRow(COLUMN_COUNT, String.valueOf(myCollec.estimatedDocumentCount()));

        if (info.getOptions().getDefaultId() != null) {
            sht.addPropertyRow(COLUMN_UUID, info.getOptions().getDefaultId().getType());
        }

        if (info.getOptions().getIndexing() != null) {
            sht.addPropertyRow("", "");
            sht.addPropertyRow(AnsiUtils.yellow("Indexing:"), "");
            List<String> allows = info.getOptions().getIndexing().getAllow();
            String allowsStr = "--";
            if (allows != null && !allows.isEmpty()) {
                allowsStr = allows.toString();
            }
            List<String> denies = info.getOptions().getIndexing().getDeny();
            String deniesStr = "--";
            if (denies != null && !denies.isEmpty()) {
                deniesStr = denies.toString();
            }
            sht.addPropertyRow(COLUMN_ALLOWS, allowsStr);
            sht.addPropertyRow(COLUMN_DENIES, deniesStr);
        }

        if (info.getOptions() != null) {
            if (info.getOptions().getVector() != null) {
                sht.addPropertyRow("", "");
                sht.addPropertyRow(AnsiUtils.yellow("Vector:"), "");
                CollectionOptions.VectorOptions vOptions = info.getOptions().getVector();
                sht.addPropertyRow(COLUMN_DIMENSION, String.valueOf(vOptions.getDimension()));
                sht.addPropertyRow(COLUMN_METRIC, vOptions.getMetric());
                if (vOptions.getService() != null) {
                    sht.addPropertyRow("", "");
                    sht.addPropertyRow(AnsiUtils.yellow("Vectorize:"), "");
                    sht.addPropertyRow(COLUMN_SERVICE, vOptions.getService().getProvider());
                    sht.addPropertyRow(COLUMN_MODEL, vOptions.getService().getModelName());
                    Map<String, Object> auths = vOptions.getService().getAuthentication();
                    if (auths != null && !auths.isEmpty()) {
                        sht.addPropertyRow(COLUMN_AUTHENTICATION, "");
                        auths.forEach((k, v) -> sht.addPropertyRow("- " + k, v.toString()));
                    } else {
                        sht.addPropertyRow(COLUMN_AUTHENTICATION, "--");
                    }
                    Map<String, Object> params = vOptions.getService().getParameters();
                    if (params != null && !params.isEmpty()) {
                        sht.addPropertyRow(COLUMN_PARAMETERS, "");
                        params.forEach((k, v) -> sht.addPropertyRow("- " + k, v.toString()));
                    } else {
                        sht.addPropertyRow(COLUMN_PARAMETERS, "");
                    }
                }
            }
        }
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Describe an embedding provider.
     *
     * @param databaseName
     *      database name
     * @param key
     *      provider key
     */
    public void describeEmbeddingProvider(String databaseName, String key) {
        Map<String, EmbeddingProvider> providers = dbDao
                .getDataAPIDatabase(databaseName)
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders();
        if (!providers.containsKey(key)) {
            throw new IllegalArgumentException("Embedding provider '" + key + "' has not been found");
        }
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow(COLUMN_KEY, key);
        EmbeddingProvider embeddingProvider = providers.get(key);
        if (embeddingProvider.getDisplayName() != null) {
            sht.addPropertyRow(COLUMN_DISPLAY_NAME, embeddingProvider.getDisplayName());
        }

        if (embeddingProvider.getParameters() != null) {
            sht.addPropertyListRows("Parameters", embeddingProvider.getParameters().stream().map(param -> {
                StringBuilder sb = new StringBuilder();
                sb.append(param.getName());
                if (param.getType() != null) {
                    sb.append(" (").append(param.getType() + ") ");
                }
                if (param.getDefaultValue() != null && !param.getDefaultValue().isEmpty()) {
                    sb.append(", defaultValue=").append(param.getDefaultValue());
                }
                return sb.toString();
            }).toList());
        }

        sht.addPropertyRow(AnsiUtils.yellow("Models:"), "");
        embeddingProvider.getModels().forEach(model -> {
            sht.addPropertyRow(COLUMN_NAME, model.getName());
            if (model.getVectorDimension() != null) {
                sht.addPropertyRow(COLUMN_DIMENSION, String.valueOf(model.getVectorDimension()));
            }
            if (model.getParameters() != null) {
                AtomicInteger i = new AtomicInteger(0);
                sht.addPropertyListRows(COLUMN_PARAMETERS, model.getParameters().stream().map(param -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(param.getName());
                    if (param.getType() != null) {
                        sb.append(" (").append(param.getType() + ") ");
                    }
                    if (param.getDefaultValue() != null && !param.getDefaultValue().isEmpty()) {
                        sb.append(", defaultValue=").append(param.getDefaultValue());
                    }
                    return sb.toString();
                } ).toList());
            }
        });
        embeddingProvider.getHeaderAuthentication().ifPresent(auth -> {
            sht.addPropertyRow(AnsiUtils.yellow("Header Auth:"), "");
            sht.addPropertyListRows("Tokens", auth.getTokens().stream().map(tok -> {
                return "forwarded=" + tok.getForwarded() + ", accepted=" + tok.getAccepted();
            }).toList());;
        });

        embeddingProvider.getSharedSecretAuthentication().ifPresent(auth -> {
            sht.addPropertyRow(AnsiUtils.yellow("Shar.Secret Auth:"), "");
            sht.addPropertyListRows("Tokens", auth.getTokens().stream().map(tok -> {
                return "forwarded=" + tok.getForwarded() + ", accepted=" + tok.getAccepted();
            }).toList());;
        });

        if (embeddingProvider.getSupportedAuthentication() != null) {
            sht.addPropertyRow(AnsiUtils.yellow("Supported Auth:"), "");
            sht.addPropertyListRows(COLUMN_AUTHENTICATION, embeddingProvider.getSupportedAuthentication().entrySet().stream().map(entry -> {
                return entry.getKey() + ": " + entry.getValue().getTokens();
            }).toList());
        }


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
            options.vectorize(cco.embeddingProvider(), cco.embeddingModel(), cco.embeddingKey());
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
