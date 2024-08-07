package com.dtsx.astra.cli.iam.user;

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

import com.dtsx.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display roles.
 */
@Command(name = "list", description = "Display the list of Users in an organization")
public class UserListCmd extends AbstractConnectedCmd {
   
    /** {@inheritDoc} */
    public void execute() {
        ServiceUser.getInstance().listUsers();
    }
    
}
