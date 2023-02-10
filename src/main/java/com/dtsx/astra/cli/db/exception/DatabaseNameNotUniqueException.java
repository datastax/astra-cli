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
 * Business exception if multiple database names.
 */
public class DatabaseNameNotUniqueException extends RuntimeException {

    /** 
     * Serial
     */
    @Serial
    private static final long serialVersionUID = -7880080384291100885L;
   
    /**
     * Constructor with dbName
     * 
     * @param dbName
     *      db name
     */
    public DatabaseNameNotUniqueException(String dbName) {
        super(("Multiple databases with same name '%s' detected. " +
               "Please fallback to database id to resolve the conflict").formatted(dbName));

    }
    
}
