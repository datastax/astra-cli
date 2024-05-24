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
import com.dtsx.astra.cli.db.AbstractDatabaseCmd;
import com.dtsx.astra.cli.db.keyspace.ServiceKeyspace;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Show Keyspaces for Databases.
 */
@Command(name = "list-collections", description = "Display the list of collections in an database")
public class DbListCollectionsCmd extends AbstractDatabaseCmd {

    /**
     * Collection creation options.
     */
    @Option(name = {"-k", "--keyspace" },
            title = "KEYSPACE",
            arity = 1,
            description = "Name of the keyspace to create the collection")
    public String keyspace = AstraDBAdmin.DEFAULT_NAMESPACE;

    /** {@inheritDoc} */
    public void execute() {
        ServiceCollection.getInstance().listCollections(db, keyspace);
    }

}
