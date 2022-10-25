package com.datastax.astra.cli.iam;

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

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.iam.exception.RoleNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display role.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "get", description = "Show role details")
public class RoleGetCmd extends AbstractConnectedCmd {
    
    /** Role name or id. */
    @Required
    @Arguments(title = "ROLE", description = "Role name or identifier")
    String role;
    
    /** {@inheritDoc} */
    public void execute() throws RoleNotFoundException {
        OperationIam.showRole(role);
    }
    
}
