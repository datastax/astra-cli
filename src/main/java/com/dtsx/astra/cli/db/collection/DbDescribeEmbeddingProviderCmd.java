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

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.db.AbstractDatabaseCmdAsync;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Describe an embedding provider
 */
@Command(name = "describe-embedding-provider", description = "Describe an existing embedding provider")
public class DbDescribeEmbeddingProviderCmd extends AbstractDatabaseCmdAsync {

    /**
     * Collection creation options.
     */
    @Required
    @Option(name = {"-ep", "--embedding-provider" },
            title = "EMBEDDING_PROVIDER",
            arity = 1,
            description = "Key of the embedding provider")
    public String emdeddingProvider;

    /** {@inheritDoc}  */
    public void executeAsync() {
        ServiceCollection.getInstance().describeEmbeddingProvider(db, emdeddingProvider);
    }
    
}
