package com.dtsx.astra.cli.db.cdc;

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
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Delete a DB is exist.
 */
@Command(name = "delete-cdc", description = "Delete a CDC connection")
public class DbDeleteCdcCmd extends AbstractDatabaseCmd {

    /**
     * Cdc Identifier
     */
    @Option(name = { "-id" }, title = "CDC_ID", arity = 1,
            description = "Identifier of the cdc when providing")
    protected String cdc;

    /**
     * keyspace.
     */
    @Option(name = { "-k", "--keyspace" }, title = "KEYSPACE", arity = 1, description = "Keyspace name")
    protected String keyspace;

    /**
     * table name.
     */
    @Option(name = { "--table" }, title = "TABLE", arity = 1, description = "Table name")
    protected String table;

    /**
     * tenant name.
     */
    @Option(name = { "--tenant" }, title = "TENANT", arity = 1, description = "Tenant name")
    protected String tenant;

    /** {@inheritDoc} */
    public void execute() {
        if (cdc != null && !cdc.equals("")) {
            LoggerShell.info("Deleting cdc with id:'%s'".formatted(cdc));
            ServiceCdc.getInstance().deleteCdcById(db, cdc);
        } else {
            LoggerShell.info("Deleting cdc from its full definition keyspace/table/tenant");
            ServiceCdc.getInstance().deleteCdcByDefinition(db, keyspace, table, tenant);
        }

    }
    
}
