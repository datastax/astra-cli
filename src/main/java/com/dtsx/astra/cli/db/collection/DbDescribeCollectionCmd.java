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

import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.db.AbstractDatabaseCmdAsync;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a collection if it exists
 */
@Command(name = "describe-collection", description = "Describe an existing collection")
public class DbDescribeCollectionCmd extends AbstractDatabaseCmdAsync {

    /**
     * Collection creation options.
     */
    @Required
    @Option(name = {"-c", "--collection" },
            title = "COLLECTION",
            arity = 1,
            description = "Name of the collection")
    public String collection;

    /**
     * Collection creation options.
     */
    @Option(name = {"-k", "--keyspace" },
            title = "KEYSPACE",
            arity = 1,
            description = "Name of the keyspace to create the collection")
    public String keyspace = DataAPIClientOptions.DEFAULT_KEYSPACE;

    /** {@inheritDoc}  */
    public void executeAsync() {
        if (ServiceCollection.getInstance().isCollectionExists(db, keyspace, collection)) {
            ServiceCollection.getInstance().describeCollection(db, keyspace, collection);
        } else {
            throw new InvalidArgumentException("Collection '%s' does not exist in '%s'.".formatted(collection, db));
        }
    }
    
}
