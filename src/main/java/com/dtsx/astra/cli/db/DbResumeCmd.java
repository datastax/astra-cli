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
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Delete a DB is exist.
 */
@Command(name = "resume", description = "Resume a db if needed")
public class DbResumeCmd extends AbstractDatabaseCmd {
    
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
            description = "Provide a limit to the wait period in seconds, default is 180s.")
    protected int timeout = 180;

    /**
     * Will not wait for the database become available.
     */
    @Option(name = { "--async" }, description = "Will not wait for the resource to become available")
    protected boolean async = false;
    
    /** {@inheritDoc}  */
    public void execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException,
            InvalidDatabaseStateException {
        dbServices.resumeDb(db);
        if (!async) {
            switch (dbServices.waitForDbStatus(db, DatabaseStatusType.ACTIVE, timeout)) {
                case NOT_FOUND -> throw new DatabaseNotFoundException(db);
                case UNAVAILABLE -> throw new InvalidDatabaseStateException(db, DatabaseStatusType.ACTIVE, DatabaseStatusType.HIBERNATED);
                default -> LoggerShell.success("Database '%s' has resumed".formatted(db));
            }
        }
    }
    
}
