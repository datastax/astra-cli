package com.dtsx.astra.cli.iam.user.exception;

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
 * User not found.
 */
public class UserNotFoundException extends RuntimeException {

    /** Serial Number. */
    @Serial
    private static final long serialVersionUID = -1134966974107948087L;
    
    /**
     * Constructor with userName
     * 
     * @param userName
     *      name of user
     */
    public UserNotFoundException(String userName) {
        super("User " + userName + "' has not been found.");
    }

}
