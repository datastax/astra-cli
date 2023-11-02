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

import io.stargate.sdk.json.domain.SimilarityMetric;

/**
 * Option for creating a collection.
 */
public record CreateCollectionOption(

        /* Name of the collection to create. */
        String collection,

        /* Name of the dimension to create. */
        Integer dimension,

        /* Name of the metric to create. */
        SimilarityMetric metric) {}
