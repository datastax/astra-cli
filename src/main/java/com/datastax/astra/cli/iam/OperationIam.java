package com.datastax.astra.cli.iam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.core.out.JsonOutput;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.ShellTable;
import com.datastax.astra.cli.iam.exception.RoleNotFoundException;
import com.datastax.astra.cli.iam.exception.UserAlreadyExistException;
import com.datastax.astra.cli.iam.exception.UserNotFoundException;
import com.datastax.astra.sdk.organizations.OrganizationsClient;
import com.datastax.astra.sdk.organizations.domain.Role;
import com.datastax.astra.sdk.organizations.domain.User;
import com.datastax.astra.sdk.utils.IdUtils;

/**
 * Utility class for command `eolw`
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class OperationIam {
    
    /** worki with roles. */
    public static final String COMMAND_ROLE = "role";
    /** worki with users. */
    public static final String COMMAND_USER = "user";
    
    /** Column name. */
    private static final String COLUMN_ROLE_ID          = "Role Id";
    /** Column name. */
    private static final String COLUMN_ROLE_NAME        = "Role Name";
    /** Column name. */
    private static final String COLUMN_ROLE_DESCRIPTION = "Description";
    
    /** Column name. */
    private static final String COLUMN_USER_ID          = "User Id";
    /** Column name. */
    private static final String COLUMN_USER_EMAIL       = "User Email";
    /** Column name. */
    private static final String COLUMN_USER_STATUS      = "Status";
    
    /**
     * List Roles.
     */
    public static void listRoles() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_ROLE_ID, 37);
        sht.addColumn(COLUMN_ROLE_NAME, 20);
        sht.addColumn(COLUMN_ROLE_DESCRIPTION, 20);
        CliContext.getInstance()
                    .getApiDevopsOrganizations()
                    .roles()
                    .forEach(role -> {
             Map <String, String> rf = new HashMap<>();
             rf.put(COLUMN_ROLE_ID, role.getId());
             rf.put(COLUMN_ROLE_NAME, role.getName());
             rf.put(COLUMN_ROLE_DESCRIPTION, role.getPolicy().getDescription());
             sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }
    
    /**
     * List Roles.
     * 
     * @param cmd
     *      current command
     */
    public static void listUsers(AbstractCmd cmd) {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_USER_ID, 37);
        sht.addColumn(COLUMN_USER_EMAIL, 20);
        sht.addColumn(COLUMN_USER_STATUS, 20);
        CliContext.getInstance()
                    .getApiDevopsOrganizations()
                    .users().forEach(user -> {
             Map <String, String> rf = new HashMap<>();
             rf.put(COLUMN_USER_ID, user.getUserId());
             rf.put(COLUMN_USER_EMAIL, user.getEmail());
             rf.put(COLUMN_USER_STATUS, user.getStatus().name());
             sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }
    
    /**
     * Show Role details.
     *
     * @param role
     *      role name
     * @throws RoleNotFoundException
     *      role has not been found
     */
    public static void showRole(String role) throws RoleNotFoundException {
        Optional<Role> optRole = CliContext
                .getInstance()
                .getApiDevopsOrganizations()
                .findRoleByName(role);
            
        if (!optRole.isPresent() && IdUtils.isUUID(role)) {
            optRole = CliContext
                 .getInstance()
                 .getApiDevopsOrganizations()
                 .role(role)
                 .find();
            }
            
        if (!optRole.isPresent()) {
            throw new RoleNotFoundException(role);
        }
            
        Role r = optRole.get();
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow("Identifier",    r.getId());
        sht.addPropertyRow("Name",          r.getName());
        sht.addPropertyRow("Description",   r.getPolicy().getDescription());
        sht.addPropertyRow("Effect",        r.getPolicy().getEffect());
        switch(CliContext.getInstance().getOutputFormat()) {
            case csv:
                sht.addPropertyRow("Resources", r.getPolicy().getResources().toString());
                sht.addPropertyRow("Actions", r.getPolicy().getActions().toString());
                AstraCliConsole.printShellTable(sht);
            break;
            case json:
                AstraCliConsole.printJson(new JsonOutput(ExitCode.SUCCESS, 
                            OperationIam.COMMAND_ROLE + " " + AbstractCmd.GET + " " + role, r));
            break;
            case human:
            default:
                sht.addPropertyListRows("Resources", r.getPolicy().getResources());
                sht.addPropertyListRows("Actions",   r.getPolicy().getActions());
                AstraCliConsole.printShellTable(sht);
            break;
        }
    }
    
    /**
     * Show User details.
     *
     * @param user
     *      user email
     * @throws UserNotFoundException
     *      user has not been found
     */
    public static void showUser(String user) throws UserNotFoundException {
       Optional<User> optUser = CliContext
               .getInstance()
               .getApiDevopsOrganizations()
               .findUserByEmail(user);
            
       if (!optUser.isPresent() && IdUtils.isUUID(user)) {
           optUser = CliContext
                .getInstance()
                .getApiDevopsOrganizations()
                .user(user)
                .find();
       }
            
       if (!optUser.isPresent()) {
           throw new UserNotFoundException(user);
       }
       
       User r = optUser.get();
       ShellTable sht = ShellTable.propertyTable(15, 40);
       sht.addPropertyRow("Identifier",   r.getUserId());
       sht.addPropertyRow("Email",        r.getEmail());
       sht.addPropertyRow("Status",       r.getStatus().name());
       
       List<String> roleNames =  r.getRoles()
               .stream()
               .map(Role::getName)
               .collect(Collectors.toList());
            
       switch(CliContext.getInstance().getOutputFormat()) {
           case csv:
               sht.addPropertyRow("Roles", roleNames.toString());
               AstraCliConsole.printShellTable(sht);
           break;
           case json:
               AstraCliConsole.printJson(new JsonOutput(ExitCode.SUCCESS, "user show " + user, r));
           break;
           case human:
           default:
               sht.addPropertyListRows("Roles", roleNames);
               AstraCliConsole.printShellTable(sht);
           break;
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
    public static void inviteUser(String user, String role) throws UserAlreadyExistException, RoleNotFoundException {
        OrganizationsClient oc = CliContext.getInstance().getApiDevopsOrganizations();
        Optional<User> optUser = oc.findUserByEmail(user);
        if (optUser.isPresent()) {
            throw new UserAlreadyExistException(user);
        }
        Optional<Role> optRole = oc.findRoleByName(role);
        if (!optRole.isPresent() && IdUtils.isUUID(role)) {
            optRole = oc.role(role).find();
        }
        if (!optRole.isPresent()) {
            throw new RoleNotFoundException(role);
        }
        oc.inviteUser(user, optRole.get().getId());
        AstraCliConsole.outputSuccess(role);
    }

    /**
     * Delete a user if exist.
     * 
     * @param cmd
     *      current command options
     * @param user
     *      user email of technial identifier
     * @throws UserNotFoundException
     *      user not found
     */
    public static void deleteUser(AbstractCmd cmd, String user) 
    throws UserNotFoundException {
        OrganizationsClient oc = CliContext.getInstance().getApiDevopsOrganizations();
        Optional<User> optUser = oc.findUserByEmail(user);
        if (!optUser.isPresent() && IdUtils.isUUID(user)) {
            optUser = oc.user(user).find();
        }
        if (!optUser.isPresent()) {
            throw new UserNotFoundException(user);
        }
        oc.user(optUser.get().getUserId()).delete();
        AstraCliConsole.outputSuccess("Deleting user '" + user + "' (async operation)");
    }
    
}
