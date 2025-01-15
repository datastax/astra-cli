package com.dtsx.astra.cli.db.table;

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
import com.dtsx.astra.cli.db.AbstractDatabaseCmdAsync;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a collection if it exists
 */
@Command(name = "delete-table", description = "Delete an existing table")
public class DbDeleteTableCmd extends AbstractDatabaseCmdAsync {

    /**
     * Table
     */
    @Required
    @Option(name = {"-t", "--table" },
            title = "TABLE",
            arity = 1,
            description = "Name of the table")
    public String table;

    /**
     * Collection creation options.
     */
    @Option(name = {"-k", "--keyspace" },
            title = "KEYSPACE",
            arity = 1,
            description = "Name of the keyspace to delete the table")
    public String keyspace = DataAPIClientOptions.DEFAULT_KEYSPACE;

    /**
     * Database or keyspace are created when needed
     **/
    @Option(name = { "--if-exists"},
            description = "will delete the table only if it exists")
    protected boolean ifExist = false;

    /** {@inheritDoc}  */
    public void executeAsync() {
        ServiceTables.getInstance().deleteTable(db, keyspace, table, ifExist);
    }
    
}
