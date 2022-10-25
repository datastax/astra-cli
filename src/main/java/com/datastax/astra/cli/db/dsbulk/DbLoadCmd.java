package com.datastax.astra.cli.db.dsbulk;

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

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Load data into AstraDB.
 * 
 * @author Cedrick LUNVEN (@clunven)
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
    
    /** {@inheritDoc} */
    @Override
    public void execute()  {
        DsBulkService.getInstance().load(this);
    }

}
