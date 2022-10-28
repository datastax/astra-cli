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
 * Cannot create or read files on hard disk drive
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class FileSystemException extends RuntimeException {

    /** Serial. */
    @Serial
    private static final long serialVersionUID = -1631087992604077795L;

    /**
     * Constructor with token
     * 
     * @param msg
     *       error message
     */
    public FileSystemException(String msg) {
        super(msg);
    }

    /**
     * Constructor with token
     *
     * @param e
     *      parent exception
     * @param msg
     *       error message
     */
    public FileSystemException(String msg, Exception e) {
        super(msg, e);
    }

}
