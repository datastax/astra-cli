package com.dtsx.astra.cli.db.exception;

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

import java.io.Serial;

/**
 * Keyspace already exists.
 */
public class DatabaseAlreadyExistException extends RuntimeException {

    /** Serial Number. */
    @Serial
    private static final long serialVersionUID = 968018206118357644L;

    /**
     * Constructor with database name
     *
     * @param dbname
     *      database name
     */
    public DatabaseAlreadyExistException(String dbname) {
        super(("A database with name '%s' already exists in the organization. " +
               "To prevent conflicts CLI will not create another one, please change name").formatted(dbname));
    }

}
