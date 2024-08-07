package com.dtsx.astra.cli.core.exception;

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

import org.fusesource.jansi.AnsiConsole;

import java.io.Serial;

import static com.datastax.astra.internal.utils.AnsiUtils.cyan;
import static com.datastax.astra.internal.utils.AnsiUtils.yellow;

/**
 * Tenant not found.
 */
public class TokenNotFoundException extends RuntimeException {
    
    /** serial. */
    @Serial
    private static final long serialVersionUID = -5461243744804311589L;

    /**
     * Default constructor
     */
    public TokenNotFoundException() {
        super("Token has not been found.");
    }

    /**
     * Default constructor
     */
    public TokenNotFoundException(String section, String tokenId) {
        super("Configuration " + yellow(section) + " has not been found. \n- list available: "
                + cyan("astra config list")
                + " \n- create new: "+ cyan("astra help config create"));
    }

    /**
     * Default constructor.
     *
     * @param tokenId
     *      token identifier
     */
    public TokenNotFoundException(String tokenId) {
        super("Token "+ tokenId + " has not been found.");
    }

}
