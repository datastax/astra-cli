package com.dtsx.astra.cli.core.out;

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

import com.dtsx.astra.cli.core.ExitCode;

/**
 * Wrapper for Json outputs.
 */
public class JsonOutput<T> {
    
    /**
     * Returned code.
     */
    private int code = ExitCode.SUCCESS.getCode();
    
    /**
     * Returned message
     */
    private final String message;
    
    /**
     * Custom payload
     */
    private T data;

    /**
     * Constructor with fields.
     *
     * @param code
     *      returned code
     * @param message
     *      returned message
     */
    public JsonOutput(ExitCode code, String message) {
        super();
        if (code != null) {
            this.code = code.getCode();
        }
        this.message = message;
    }

    /**
     * Constructor with fields.
     *
     * @param code
     *      returned code
     * @param message
     *      returned message
     * @param data
     *      data in JSON
     */
    public JsonOutput(ExitCode code, String message, T data) {
        this(code, message);
        this.data     = data;  
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

    /**
     * Setter accessor for attribute 'code'.
     * @param code
     * 		new value for 'code '
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Getter accessor for attribute 'message'.
     *
     * @return
     *       current value of 'message'
     */
    public String getMessage() {
        return message;
    }

    /**
     * Getter accessor for attribute 'data'.
     *
     * @return
     *       current value of 'data'
     */
    public Object getData() {
        return data;
    }

    /**
     * Setter accessor for attribute 'data'.
     * @param data
     * 		new value for 'data '
     */
    public void setData(T data) {
        this.data = data;
    }
    

}
