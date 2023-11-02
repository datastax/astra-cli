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

import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.AbstractDatabaseCmdAsync;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.cli.db.keyspace.ServiceKeyspace;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.stargate.sdk.json.domain.SimilarityMetric;

/**
 * Creating a new collection.
 */
@Command(name = "create-collection", description = "Create a new collection")
public class DbCreateCollectionCmd extends AbstractDatabaseCmdAsync {

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
    public String metric = SimilarityMetric.cosine.name();

    /** {@inheritDoc}  */
    public void executeAsync() {
        if (dimension != null && dimension < 1) {
            throw new InvalidArgumentException("Dimension must be a positive integer.");
        }
        ServiceCollection.getInstance()
                .createCollection(db, new CreateCollectionOption(collection, dimension,
                        AstraCliUtils.lookupMetric(metric)));
    }
    
}
