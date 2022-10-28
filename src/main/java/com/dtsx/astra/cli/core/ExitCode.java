package com.dtsx.astra.cli.core;

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

/**
 * Normalization of exit codes.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public enum ExitCode {
    
    /** code ok. */
    SUCCESS(0),

    /** code. */
    UNAVAILABLE(2),

    /** code. */
    INVALID_PARAMETER(4),
    
    /** code. */
    NOT_FOUND(5),

    /** conflict. */
    ALREADY_EXIST(7),
    
    /** code. */
    CANNOT_CONNECT(8),

    /** code. */
    CONFIGURATION(9), 

    /** code. */
    INVALID_ARGUMENT(11),
    
    /** code. */
    INVALID_OPTION(12),
    
    /** code. */
    INVALID_OPTION_VALUE(13),
    
    /** code. */
    UNRECOGNIZED_COMMAND(14),
    
    /** Internal error. */
    INTERNAL_ERROR(100);
    
    /* Exit code. */
    private final int code;
    
    /**
     * Constructor.
     *
     * @param code
     *      target code
     */
    ExitCode(int code) {
        this.code = code;
    }

    /**
     * Getter accessor for attribute 'code'.
     *
     * @return
     *       current value of 'code'
     */
    public int getCode() {
        return code;
    }

}
