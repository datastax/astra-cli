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

import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.db.AbstractDatabaseCmd;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a region from a db.
 */
@Command(name = "delete-region", description = "Delete a region from a database")
public class DbDeleteRegionCmd extends AbstractDatabaseCmd {

    /** Provide a keyspace Name. */
    @Required
    @Option(name = {"-r", "--region" },
            title = "REGION",
            arity = 1,
            description = "Name of the region to create")
    public String region;

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
            description = "Max wait time in seconds, default+1800, 0 = no timeout.")
    protected int timeout = 1800;

    /**
     * Will wait until the database become ACTIVE.
     */
    @Option(name = { "--async" },
            description = "Will wait until the database become ACTIVE")
    protected boolean async = false;

    /** {@inheritDoc}  */
    public void execute() {
        ServiceRegion.getInstance().deleteRegion(db, region);
        if (!async) {
            if (ServiceRegion.getInstance().retryUntilRegionDeleted(db, region, timeout) >= timeout) {
                throw new InvalidDatabaseStateException(db, DatabaseStatusType.MAINTENANCE, DatabaseStatusType.ACTIVE);
            } else {
                AstraCliConsole.outputSuccess("Region %s has been deleted".formatted(region));
            }
        }
    }
}
