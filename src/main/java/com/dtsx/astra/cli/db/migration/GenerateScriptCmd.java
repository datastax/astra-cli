package com.dtsx.astra.cli.db.migration;

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
import com.github.rvesse.airline.annotations.Option;

/**
 * Generate a script to implement a migration
 */
@Command(name = "generate-script", description = "Generate script files for a keyspace")
public class GenerateScriptCmd extends AbstractDatabaseCmd {

    /** Cqlsh Options. */
    @Option(name = {"-k", "--keyspaces" }, title = "KEYSPACES", arity = 1, description = "list of keyspaces to enter")
    protected String keyspaces;

    /** Cqlsh Options. */
    @Option(name = {"-t", "--tables" }, title = "TABLES", arity = 1, description = "list of tables to enter")
    protected String tables;

    /** Cqlsh Options. */
    @Option(name = {"-d", "--data-dir" }, title = "DATA DIRECTORY", arity = 1,
            description = "The directory where CQL files will be generated.")
    protected String dataDir;



    /** {@inheritDoc} */
    public void execute() {
        ServiceDsBulkMigrator
                .getInstance()
                .generateDDL(new GenerateDdlOptions(db, dataDir, keyspaces, tables));
    }


}
