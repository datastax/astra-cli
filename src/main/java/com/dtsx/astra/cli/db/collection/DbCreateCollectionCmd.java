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

import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.AbstractDatabaseCmdAsync;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Creating a new collection.
 */
@Command(name = "create-collection", description = "Create a new collection")
public class DbCreateCollectionCmd extends AbstractDatabaseCmdAsync {

    /**
     * Database or keyspace are created when needed
     **/
    @Option(name = { "--if-not-exists" },
            description = "will create a new collection only if none with same name")
    protected boolean ifNotExist = false;

    /**
     * Collection creation options.
     */
    @Required
    @Option(name = {"-c", "--collection" },
            title = "COLLECTION",
            arity = 1,  
            description = "Name of the collection to create")
    public String collection;

    /**
     * Collection creation options.
     */
    @Option(name = {"-k", "--keyspace" },
            title = "KEYSPACE",
            arity = 1,
            description = "Name of the keyspace to create the collection")
    public String keyspace = DataAPIClientOptions.DEFAULT_KEYSPACE;

    /**
     * Collection creation options.
     */
    @Option(name = {"-d", "--dimension" },
            title = "DIMENSION",
            arity = 1,
            description = "Name of the collection to create")
    public Integer dimension;

    /**
     * Collection creation options.
     */
    @Option(name = {"-m", "--metric" },
            title = "METRIC",
            arity = 1,
            description = "Name of the collection to create")
    public String metric;

    /**
     * Collection creation options.
     */
    @Option(name = {"--indexing-allow" },
            title = "INDEXING_ALLOW",
            arity = 1,
            description = "List of attribute to add into index (comma separated)")
    public String indexAllow;

    /**
     * Collection creation options.
     */
    @Option(name = {"--indexing-deny" },
            title = "INDEXING_DENY",
            arity = 1,
            description = "List of attribute to remove from index (comma separated)")
    public String indexDeny;

    /**
     * Collection creation options.
     */
    @Option(name = {"--default-id" },
            title = "DEFAULT_ID",
            arity = 1,
            description = "Default identifier to use for the collection")
    public String defaultId;

    /**
     * Collection creation options.
     */
    @Option(name = {"--embedding-provider" },
            title = "EMBEDDING_PROVIDER",
            arity = 1,
            description = "Using Vectorize, embedding provider to use")
    public String embeddingProvider;

    /**
     * Collection creation options.
     */
    @Option(name = {"--embedding-key" },
            title = "EMBEDDING_KEY",
            arity = 1,
            description = "Using Vectorize, embedding key used for shared secret")
    public String embeddingKey;

    /**
     * Collection creation options.
     */
    @Option(name = {"--embedding-model" },
            title = "EMBEDDING_MODEL",
            arity = 1,
            description = "Using Vectorize, embedding mode to use")
    public String embeddingModel;

    /** {@inheritDoc}  */
    public void executeAsync() {
        // Parameters Validation at Command Level
        if (dimension != null && dimension < 1) {
            throw new InvalidArgumentException("Dimension must be a positive integer.");
        }
        if (indexAllow!= null && indexDeny != null) {
            throw new InvalidArgumentException("Cannot have both --indexing-deny and --indexing-allow");
        }
        if (embeddingModel != null && embeddingProvider == null) {
            throw new IllegalArgumentException("Cannot have --embedding-model without --embedding-provider");
        }
        if (embeddingModel == null && embeddingProvider != null) {
            throw new IllegalArgumentException("Cannot have --embedding-provider without --embedding-model");
        }
        // Defaulting Metric if a dimension is provided
        if (dimension != null && metric == null) {
            metric = SimilarityMetric.COSINE.name();
        }
        com.datastax.astra.client.collections.Collection<Document> col = ServiceCollection.getInstance()
                .createCollection(db, keyspace, new CollectionCreationOptions(
                        collection, dimension,
                        AstraCliUtils.parseMetric(metric),
                        AstraCliUtils.parseIndex(indexAllow),
                        AstraCliUtils.parseIndex(indexDeny),
                        AstraCliUtils.parseDefaultId(defaultId),
                        embeddingModel, embeddingProvider, embeddingKey, ifNotExist));
        LoggerShell.success("Collection '%s' as been created from db '%s' on keyspace  '%s'".formatted(collection, db, keyspace));
    }
    
}
