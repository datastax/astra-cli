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

import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.cli.db.exception.KeyspaceAlreadyExistException;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Create a Database.
 */
@Command(name = "create", description = "Create a database with cli")
public class DbCreateCmd extends AbstractDatabaseCmd {
    
    /** 
     * Database or keyspace are created when needed
     **/
    @Option(name = { "--if-not-exist", "--if-not-exists" }, 
            description = "will create a new DB only if none with same name")
    protected boolean ifNotExist = false;

    /**
     * Cloud provider region to provision
     */
    @Option(name = { "-r", "--region" }, title = "DB_REGION", arity = 1, 
            description = "Cloud provider region to provision")
    protected String region = ServiceDatabase.DEFAULT_REGION;
    
    /**
     * Default keyspace created with the Db
     */
    @Option(name = { "-k", "--keyspace" }, title = "KEYSPACE", arity = 1, 
            description = "Default keyspace created with the Db")
    protected String keyspace;
    
    /** 
     * Will wait until the database become ACTIVE.
     */
    @Option(name = { "--wait" }, 
            description = "Will wait until the database become ACTIVE")
    protected boolean wait = true;

    /**
     * Will not wait for the database become available.
     */
    @Option(name = { "--async" }, description = "Will not wait for the resource to become available")
    protected boolean async = false;
    
    /** 
     * Provide a limit to the wait period in seconds, default is 180s. 
     */
    @Option(name = { "--timeout" }, 
            description = "Provide a limit to the wait period in seconds, default is 300s.")
    protected int timeout = ServiceDatabase.DEFAULT_TIMEOUT_SECONDS;
    
    /** {@inheritDoc} */
    @Override
    public void execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException,
            InvalidDatabaseStateException, InvalidArgumentException, KeyspaceAlreadyExistException {
        dbServices.createDb(db, region, keyspace, ifNotExist);
        if (!async) {
            switch (dbServices.waitForDbStatus(db, DatabaseStatusType.ACTIVE, timeout)) {
                case NOT_FOUND -> throw new DatabaseNotFoundException(db);
                case UNAVAILABLE -> throw new InvalidDatabaseStateException(db, DatabaseStatusType.ACTIVE, DatabaseStatusType.PENDING);
                default -> LoggerShell.success("Database '%s' is ready.".formatted(db));
            }
        }
    }
    
}
