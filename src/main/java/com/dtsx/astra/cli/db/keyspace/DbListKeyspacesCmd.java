package com.dtsx.astra.cli.db.keyspace;

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

import com.dtsx.astra.cli.db.AbstractDatabaseCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Show Keyspaces for Databases.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "list-keyspaces", description = "Display the list of Keyspaces in an database")
public class DbListKeyspacesCmd extends AbstractDatabaseCmd {
    
    /** {@inheritDoc} */
    public void execute() {
        dbServices.listKeyspaces(db);
    }

}
