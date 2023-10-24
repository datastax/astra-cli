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

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.JsonOutput;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.iam.role.exception.RoleNotFoundException;
import com.dtsx.astra.cli.iam.user.exception.UserAlreadyExistException;
import com.dtsx.astra.cli.iam.user.exception.UserNotFoundException;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.org.domain.User;
import com.dtsx.astra.sdk.utils.IdUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for command `user`.
 * The singleton pattern is validated with a Lazy initialization
 * and a thread safe implementation.
 */
@SuppressWarnings("java:S6548")
public class ServiceUser {

    /** Column name. */
    private static final String COLUMN_USER_ID          = "User Id";
    /** Column name. */
    private static final String COLUMN_USER_EMAIL       = "User Email";
    /** Column name. */
    private static final String COLUMN_USER_STATUS      = "Status";

    /**
     * Singleton Pattern
     */
    private static ServiceUser instance;

    /**
     * Singleton Pattern.
     *
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceUser getInstance() {
        if (null == instance) {
            instance = new ServiceUser();
        }
        return instance;
    }

    /**
     * Default Constructor.
     */
    private ServiceUser() {}

    /**
     * Access Api devops from context.
     *
     * @return
     *      api devops
     */
    private AstraOpsClient apiDevopsOrg() {
        return CliContext.getInstance().getApiDevops();
    }

    /**
     * List Roles.
     */
    public void listUsers() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_USER_ID, 37);
        sht.addColumn(COLUMN_USER_EMAIL, 20);
        sht.addColumn(COLUMN_USER_STATUS, 20);
        apiDevopsOrg().users().findAll().forEach(user -> {
             Map <String, String> rf = new HashMap<>();
             rf.put(COLUMN_USER_ID, user.getUserId());
             rf.put(COLUMN_USER_EMAIL, user.getEmail());
             rf.put(COLUMN_USER_STATUS, user.getStatus().name());
             sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }
    
    /**
     * Show User details.
     *
     * @param user
     *      user email
     * @throws UserNotFoundException
     *      user has not been found
     */
    public void showUser(String user) throws UserNotFoundException {
       Optional<User> optUser = apiDevopsOrg().users().findByEmail(user);
            
       if (optUser.isEmpty() && IdUtils.isUUID(user)) {
           optUser = apiDevopsOrg().users().find(user);
       }
       
       User r = optUser.orElseThrow(() -> new UserNotFoundException(user));
       ShellTable sht = ShellTable.propertyTable(15, 40);
       sht.addPropertyRow(COLUMN_USER_ID,   r.getUserId());
       sht.addPropertyRow(COLUMN_USER_EMAIL,  r.getEmail());
       sht.addPropertyRow(COLUMN_USER_STATUS, r.getStatus().name());
       
       List<String> roleNames =  r.getRoles()
               .stream()
               .map(Role::getName)
               .toList();

        switch (CliContext.getInstance().getOutputFormat()) {
            case CSV -> {
                sht.addPropertyRow("Roles", roleNames.toString());
                AstraCliConsole.printShellTable(sht);
            }
            case JSON ->
                AstraCliConsole.printJson(new JsonOutput<>(ExitCode.SUCCESS, "user show " + user, r));
            case HUMAN -> {
                sht.addPropertyListRows("Roles", roleNames);
                AstraCliConsole.printShellTable(sht);
            }
        }
    }
    
    /**
     * Invite User.
     *
     * @param user
     *      user email
     * @param role
     *      target role for the user
     * @throws UserAlreadyExistException
     *      user does not exist
     * @throws RoleNotFoundException
     *      role does not exist 
     */
    public void inviteUser(String user, String role) throws UserAlreadyExistException, RoleNotFoundException {
        Optional<User> optUser = apiDevopsOrg().users().findByEmail(user);
        if (optUser.isPresent()) {
            throw new UserAlreadyExistException(user);
        }
        Optional<Role> optRole = apiDevopsOrg().roles().findByName(role);
        if (optRole.isEmpty() && IdUtils.isUUID(role)) {
            optRole = apiDevopsOrg().roles().find(role);
        }
        apiDevopsOrg().users().invite(user, optRole.orElseThrow(()-> new RoleNotFoundException(role)).getId());
        AstraCliConsole.outputSuccess(role);
    }

    /**
     * Delete a user if exist.
     * @param user
     *      user email of technical identifier
     * @throws UserNotFoundException
     *      user not found
     */
    public void deleteUser(String user)
    throws UserNotFoundException {
        Optional<User> optUser = apiDevopsOrg().users().findByEmail(user);
        if (optUser.isEmpty() && IdUtils.isUUID(user)) {
            optUser = apiDevopsOrg().users().find(user);
        }
        if (optUser.isEmpty()) {
            throw new UserNotFoundException(user);
        }
        apiDevopsOrg().users().delete(optUser.get().getUserId());
        AstraCliConsole.outputSuccess("Deleting user '" + user + "' (async operation)");
    }
    
}
