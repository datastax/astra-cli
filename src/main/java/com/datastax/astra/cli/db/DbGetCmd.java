package com.datastax.astra.cli.db;

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

import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "get", description = "Show details of a database")
public class DbGetCmd extends AbstractDatabaseCmd {

    /** Enum for db get. */
    public enum DbGetKeys { 
        /** db unique identifier */
        ID,
        /** db status */
        STATUS,
        /** cloud provider */
        CLOUD,
        /** default keyspace */
        KEYSPACE,
        /** all keyspaces */
        KEYSPACES,
        /** default region */
        REGION,
        /** all regions */
        REGIONS
    }
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-k", "--key" }, title = "Key", description = ""
            + "Show value for a property among: "
            + "'id', 'status', 'cloud', 'keyspace', 'keyspaces', 'region', 'regions'")
    protected String key;
    
    /** {@inheritDoc} */
    public void execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException {    
        dbServices.showDb(db, key == null ? null : DbGetKeys.valueOf(key.toUpperCase()));
    }

}
