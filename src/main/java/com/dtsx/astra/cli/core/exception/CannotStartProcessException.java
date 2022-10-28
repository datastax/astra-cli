package com.dtsx.astra.cli.core.exception;

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
 * Exception throws when third party process cannot start.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class CannotStartProcessException extends RuntimeException {

    /** Serial. */
    @Serial
    private static final long serialVersionUID = 3366100557983747828L;

    /**
     * Constructor with process
     * 
     * @param process
     *      process name
     * @param parent
     *      parent exception
     */
    public CannotStartProcessException(String process, Throwable parent) {
        super("Cannot start process '%s', error:%s".formatted(process, parent.getMessage()), parent);
    }
    
}
