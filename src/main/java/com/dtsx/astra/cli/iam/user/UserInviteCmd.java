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
import com.dtsx.astra.cli.iam.role.exception.RoleNotFoundException;
import com.dtsx.astra.cli.iam.user.exception.UserAlreadyExistException;
import com.dtsx.astra.sdk.org.domain.DefaultRoles;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Invite user.
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
        ServiceUser.getInstance().inviteUser(user, role);
    }

}
