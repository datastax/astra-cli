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

import com.dtsx.astra.cli.core.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Abstraction for DB Commands.
 */
public abstract class AbstractDatabaseCmd extends AbstractConnectedCmd {

     /** Access to sdb Services. */
    protected ServiceDatabase dbServices = ServiceDatabase.getInstance();
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB", description = "Database name (if unique) or Database identifier")
    protected String db;

    /**
     * Getter accessor for attribute 'db'.
     *
     * @return
     *       current value of 'db'
     */
    public String getDb() {
        return db;
    }

    /**
     * Wait for current database to get back to active state.
     *
     * @param timeout
     *      current timeout
     */
    protected void waitForDb(int timeout) {
       switch (dbServices.waitForDbStatus(db, DatabaseStatusType.ACTIVE, timeout)) {
                case NOT_FOUND -> throw new DatabaseNotFoundException(db);
                case UNAVAILABLE -> throw new InvalidDatabaseStateException(db, DatabaseStatusType.ACTIVE, DatabaseStatusType.MAINTENANCE);
                default -> LoggerShell.success("Database '%s' is ready.".formatted(db));
       }
    }
    
    
}
