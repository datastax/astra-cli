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

import com.dtsx.astra.cli.db.AbstractDatabaseCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * List regions for a DB.
 */
@Command(name = "list-regions", description = "List regions for a database")
public class DbListRegionsCmd extends AbstractDatabaseCmd {

    /** {@inheritDoc}  */
    public void execute() {
        ServiceRegion.getInstance().listRegions(db);
    }
}
