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

import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Option;

/**
 * Abstraction for DB Commands.
 */
public abstract class AbstractDatabaseCmdAsync extends AbstractDatabaseCmd {

    /**
     * Will wait until the database become ACTIVE.
     */
    @Option(name = { "--async" },
            description = "Will not wait for the database to become ACTIVE")
    protected boolean async = false;

    /**
     * Provide a limit to the wait period in seconds, default is 180s.
     */
    @Option(name = { "--timeout" },
            description = "Provide a limit to the wait period in seconds, default is 300s.")
    protected int timeout = ServiceDatabase.DEFAULT_TIMEOUT_SECONDS;

    /**
     * Function to be implemented by terminal class.
     */
    protected abstract void executeAsync();

    /**
     * Execute and then wait for the DB to become Active.
     */
    public void execute() {
        executeAsync();
        if (!async) {
            switch (dbServices.waitForDbStatus(db, DatabaseStatusType.ACTIVE, timeout)) {
                case NOT_FOUND -> throw new DatabaseNotFoundException(db);
                case UNAVAILABLE -> throw new InvalidDatabaseStateException(db, DatabaseStatusType.ACTIVE, DatabaseStatusType.MAINTENANCE);
                default -> LoggerShell.success("Database '%s' is ready.".formatted(db));
            }
        }
    }
    
}
