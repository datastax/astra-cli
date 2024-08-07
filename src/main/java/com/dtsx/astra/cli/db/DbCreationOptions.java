package com.dtsx.astra.cli.db;

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

/**
 * Group DB Creation Options as a record instead of a lot of attributes.
 */
public record DbCreationOptions(

        /* database name. */
        String databaseName,

        /* database region. */
        String databaseRegion,

        /* keyspace name. */
        String keyspaceName,

        /* database tier. */
        String tier,

        /* capacity units. */
        int capacityUnits,

        /* only execute if database does not exists. */
        boolean flagIfNotExist,

        /* enabling Vector. */
        boolean flagVector) {
}
