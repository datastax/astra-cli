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

import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.core.vector.SimilarityMetric;

/**
 * Option for creating a collection.
 */
public record CollectionCreationOptions(

        /* Name of the collection to create. */
        String name,

        /* Name of the dimension to create. */
        Integer dimension,

        /* Name of the metric to create. */
        SimilarityMetric metric,

        /* List of fields to add in indexing allow. */
        String[] indexingAllow,

        /* List of fields to add in indexing deny. */
        String[] indexingDeny,

        /* Default Id type. */
        CollectionDefaultIdTypes defaultId,

        /* Embedding model for vectorize. */
        String embeddingModel,

        /* Embedding provider for vectorize. */
        String embeddingProvider,

        /* Embedding key for vectorize. */
        String embeddingKey,

        /* only execute if database does not exists. */
        boolean flagIfNotExist) {}