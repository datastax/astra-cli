package com.dtsx.astra.cli.db.region;

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
import com.dtsx.astra.cli.db.AbstractDatabaseCmd;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Expand database to a new region.
 */
@Command(name = "create-region", description = "Expand database to a new region")
public class DbCreateRegionCmd extends AbstractDatabaseCmd {

    /** Provide a keyspace Name. */
    @Required
    @Option(name = {"-r", "--region" },
            title = "REGION",
            arity = 1,
            description = "Name of the region to create")
    public String region;

    /** Provide a cloud provider Name. */
    @Option(name = {"-c", "--cloud" },
            title = "CLOUD",
            arity = 1,
            description = "Name of the cloud provider")
    public String cloudProvider;

    /** Provide a cloud tier. */
    @Option(name = {"-t", "--tier" },
            title = "CLOUD",
            arity = 1,
            description = "Name of the tiers")
    public String tier = "serverless";

    /** Option. */
    @Option(name = { "--if-not-exist" },
            description = "will create a new DB only if none with same name")
    protected boolean ifNotExist = false;

    /**
     * Will wait until the database become ACTIVE.
     */
    @Option(name = { "--wait" },
            description = "Will wait until the database become ACTIVE")
    protected boolean wait = true;

    /**
     * Will wait until the database become ACTIVE.
     */
    @Option(name = { "--async" },
            description = "Will wait until the database become ACTIVE")
    protected boolean async = false;

    /**
     * Provide a limit to the wait period in seconds, default is 180s.
     */
    @Option(name = { "--timeout" },
            description = "Max wait time in seconds, default+1800, 0 = no timeout.")
    protected int timeout = 1800;

    /** {@inheritDoc}  */
    public void execute() {
        ServiceRegion.getInstance().addRegion(db, region, cloudProvider, tier, ifNotExist);
        if (!async) {
            switch (dbServices.waitForDbStatus(db, DatabaseStatusType.ACTIVE, timeout)) {
                case NOT_FOUND -> throw new DatabaseNotFoundException(db);
                case UNAVAILABLE -> throw new InvalidDatabaseStateException(db, DatabaseStatusType.ACTIVE, DatabaseStatusType.MAINTENANCE);
                default -> LoggerShell.success("Database '%s' is ready.".formatted(db));
            }
        }
    }
}
