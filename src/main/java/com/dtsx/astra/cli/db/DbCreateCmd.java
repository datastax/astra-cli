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
import com.dtsx.astra.cli.db.cqlsh.CqlShellOption;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationBuilder;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.db.exception.KeyspaceAlreadyExistException;
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
    protected String region = DatabaseCreationBuilder.DEFAULT_REGION;

    /**
     * Cloud provider for the db
     */
    @Option(name = { "-c", "--cloud" }, description = "Cloud Provider to create a db")
    protected String cloud = DatabaseCreationBuilder.DEFAULT_CLOUD.name().toLowerCase();
    
    /**
     * Default keyspace created with the Db
     */
    @Option(name = { "-k", "--keyspace" }, title = "KEYSPACE", arity = 1, 
            description = "Default keyspace created with the Db")
    protected String keyspace = "default_keyspace";

    /**
     * Default keyspace created with the Db
     */
    @Option(name = { "--tier" }, title = "TIER", arity = 1,
            description = "Tier to create the database in")
    protected String tier = DatabaseCreationBuilder.DEFAULT_TIER;

    /**
     * Default keyspace created with the Db
     */
    @Option(name = { "--capacity-units" }, title = "CAPACITY UNITS", arity = 1,
            description = "Capacity units to create the database with (default 1)")
    protected Integer capacityUnits = 1;

    /**
     * Will not wait for the database become available.
     */
    @Option(name = { "--async" }, description = "Will not wait for the resource to become available")
    protected boolean flagAsync = false;

    /**
     * Will not wait for the database become available.
     */
    @Option(name = { "--vector" }, description = "Create a database with vector search enabled")
    protected boolean flagVector = false;

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
           InvalidDatabaseStateException, InvalidArgumentException,
            KeyspaceAlreadyExistException {
        dbServices.validateCloudAndRegion(cloud, region, flagVector);
        dbServices.createDb(new DbCreationOptions(db, region, keyspace, tier, capacityUnits, ifNotExist, flagVector));

        if (!flagAsync) {
            switch (dbServices.waitForDbStatus(db, DatabaseStatusType.ACTIVE, timeout)) {
                case NOT_FOUND -> throw new DatabaseNotFoundException(db);
                case UNAVAILABLE -> throw new InvalidDatabaseStateException(db, DatabaseStatusType.ACTIVE, DatabaseStatusType.PENDING);
                default -> LoggerShell.success("Database '%s' is ready.".formatted(db));
            }
        }
    }
    
}
