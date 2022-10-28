package com.dtsx.astra.cli.db.exception;

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

import java.io.Serial;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabaseNotFoundException extends RuntimeException {

    /** Serial Number. */
    @Serial
    private static final long serialVersionUID = 8155558354861561721L;
    
    /**
     * Constructor with dbName
     * 
     * @param dbName
     *      db name
     */
    public DatabaseNotFoundException(String dbName) {
        super("Database '" + dbName + "' has not been found.");
    }

}
