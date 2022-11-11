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
 * Regions already exists
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class RegionAlreadyExistException extends RuntimeException {

    /** Serial Number. */
    @Serial
    private static final long serialVersionUID = 968018206118357644L;

    /**
     * Constructor with region name
     *
     * @param regionName
     *      region name
     * @param dbname
     *      database name
     */
    public RegionAlreadyExistException(String regionName, String dbname) {
        super(("Region '%s' already exists for database %s. " +
                "Cannot create another region with same name. " +
                "Use flag --if-not-exist to connect to the existing region.").formatted(regionName, dbname));
    }

}
