package com.dtsx.astra.cli.db;

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

import com.dtsx.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Abstraction for DB Commands.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractDatabaseCmd extends AbstractConnectedCmd {

     /** Access to sdb Services. */
    protected DatabaseService dbServices = DatabaseService.getInstance();
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB", description = "Database name (not unique)")
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
    
    
}
