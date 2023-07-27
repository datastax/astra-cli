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

import com.dtsx.astra.sdk.utils.ApiLocator;

import java.io.Serial;
import java.util.Arrays;

/**
 * Astra platform not found.
 */
public class AstraEnvironmentNotFoundException extends RuntimeException {

    /** serial. */
    @Serial
    private static final long serialVersionUID = -5461243744804311589L;

    /**
     * Default constructor
     */
    public AstraEnvironmentNotFoundException() {
        super("Astra Environment has not been found, must be one of " + Arrays.toString(ApiLocator.AstraEnvironment.values()));
    }
}
