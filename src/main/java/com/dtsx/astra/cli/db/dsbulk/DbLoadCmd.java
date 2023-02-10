package com.dtsx.astra.cli.db.dsbulk;

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

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Load data into AstraDB.
 */
@Command(name = "load", description = "Load data leveraging DSBulk")
public class DbLoadCmd extends AbstractDsbulkDataCmd {
    
    /**
     * Optional filter
     */
    @Option(name = { "-dryRun" },
            title = "dryRun", 
            description = "Enable or disable dry-run mode, a test mode that runs the "
                    + "command but does not load data. ")
    boolean dryRun = false;

    /**
     * Optional filter
     */
    @Option(name = { "--schema.allowMissingFields" },
            title = "allowMissingFields",
            arity = 1,
            description = "Ease the mapping")
    boolean allowMissingFields = false;
    
    /** {@inheritDoc} */
    @Override
    public void execute()  {
        if (url == null || "".equals(url)) {
            throw new IllegalArgumentException("Option 'url' is required to load data");
        }
        ServiceDsBulk.getInstance().load(this);
    }

}
