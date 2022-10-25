package com.datastax.astra.cli.db.exception;

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

import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;

import java.io.Serial;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class InvalidDatabaseStateException extends RuntimeException {

    /** Serial Number. */
    @Serial
    private static final long serialVersionUID = -8460056062064740428L;

    /**
     * Simple constructor.
     *
     * @param msg
     *      current message
     */
    public InvalidDatabaseStateException(String msg) {
        super(msg);
    }

    /**
     * Constructor with dbName
     * 
     * @param dbName
     *      database name
     * @param expected
     *      expected status
     * @param current
     *      current db status
     */
    public InvalidDatabaseStateException(String dbName, DatabaseStatusType expected, DatabaseStatusType current) {
        super("Database '" + dbName + "' has been found "
                + "but operation cannot be processed due "
                + "to invalid state (" + (current) + ") expected (" + expected + ")");
    }

}
