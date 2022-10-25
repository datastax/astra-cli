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
import com.datastax.astra.cli.iam.exception.UserAlreadyExistException;
import com.datastax.astra.sdk.organizations.domain.DefaultRoles;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Invite user.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "invite", description = "Invite a user to an organization")
public class UserInviteCmd extends AbstractConnectedCmd {

    /** identifier or email. */
    @Required
    @Arguments(title = "EMAIL", description = "User Email")
    String user;
    
    /**
     * Cloud provider region to provision
     */
    @Option(name = { "-r", "--role"}, title = "ROLE", arity = 1, 
            description = "Role for the user (default is Database Administrator)")
    protected String role = DefaultRoles.DATABASE_ADMINISTRATOR.getName();
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws UserAlreadyExistException, RoleNotFoundException {
        OperationIam.inviteUser(user, role);
    }

}
