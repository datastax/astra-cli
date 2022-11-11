package com.dtsx.astra.cli.db;

/*-
 * #%L
 * Astra Cli
 * %%
 * Copyright (C) 2022 DataStax
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "delete", description = "Delete an existing database")
public class DbDeleteCmd extends AbstractDatabaseCmd {

    /**
     * Will wait until the database become ACTIVE.
     */
    @Option(name = { "--wait" },
            description = "Will wait until the database become ACTIVE")
    protected boolean wait = true;

    /**
     * Provide a limit to the wait period in seconds, default is 180s.
     */
    @Option(name = { "--timeout" },
            description = "Provide a limit to the wait period in seconds, default is 300s.")
    protected int timeout = 300;

    /**
     * Will not wait for the database become available.
     */
    @Option(name = { "--async" }, description = "Will not wait for the resource to become available")
    protected boolean async = false;

    /** {@inheritDoc} */
    public void execute() {
        dbServices.deleteDb(db);
        if (!async) {
            if (dbServices.retryUntilDbDeleted(db, timeout) >= timeout) {
                throw new InvalidDatabaseStateException(db, DatabaseStatusType.TERMINATED,
                        DatabaseStatusType.TERMINATING);
            } else {
                AstraCliConsole.outputSuccess("Database %s has been deleted".formatted(db));
            }
        }
    }
    
}
