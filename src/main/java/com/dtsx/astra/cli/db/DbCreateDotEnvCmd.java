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

import java.nio.file.Paths;

import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Create .env file.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "create-dotenv", description = "Generate an .env configuration file associate with the db")
public class DbCreateDotEnvCmd extends AbstractDatabaseCmd {
    
    /**
     * Cloud provider region to provision
     */
    @Option(name = { "-r", "--region" }, title = "DB_REGION", arity = 1, 
            description = "Cloud provider region to provision")
    protected String region = DatabaseService.DEFAULT_REGION;
    
    /**
     * Default keyspace created with the Db
     */
    @Option(name = { "-k", "--keyspace" }, title = "KEYSPACE", arity = 1, 
            description = "Default keyspace created with the Db")
    protected String keyspace;
    
    /**
     * Default keyspace created with the Db
     */
    @Option(name = { "-d", "--directory" }, title = "DIRECTORY", arity = 1, 
            description = "Destination for the config file")
    protected String destination = Paths.get(".").toAbsolutePath().normalize().toString();
    
    /** {@inheritDoc} */
    public void execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException {
        dbServices.generateDotEnvFile(db, keyspace, region, destination);
    }

}
