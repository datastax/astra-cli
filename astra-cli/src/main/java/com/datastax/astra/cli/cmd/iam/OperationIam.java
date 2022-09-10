package com.datastax.astra.cli.cmd.iam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.cmd.AbstractCmd;
import com.datastax.astra.cli.out.JsonOutput;
import com.datastax.astra.cli.out.ShellPrinter;
import com.datastax.astra.cli.out.ShellTable;
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
     * 
     * @return
     *      returned code
     */
    public static ExitCode listRoles() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_ROLE_ID, 37);
        sht.addColumn(COLUMN_ROLE_NAME, 20);
        sht.addColumn(COLUMN_ROLE_DESCRIPTION, 20);
        ShellContext.getInstance()
                    .getApiDevopsOrganizations()
                    .roles()
                    .forEach(role -> {
             Map <String, String> rf = new HashMap<>();
             rf.put(COLUMN_ROLE_ID, role.getId());
             rf.put(COLUMN_ROLE_NAME, role.getName());
             rf.put(COLUMN_ROLE_DESCRIPTION, role.getPolicy().getDescription());
             sht.getCellValues().add(rf);
        });
        ShellPrinter.printShellTable(sht);
        return ExitCode.SUCCESS;
    }
    
    /**
     * List Roles.
     * 
     * @param cmd
     *      current command
     * @return
     *      returned code
     */
    public static ExitCode listUsers(AbstractCmd cmd) {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_USER_ID, 37);
        sht.addColumn(COLUMN_USER_EMAIL, 20);
        sht.addColumn(COLUMN_USER_STATUS, 20);
        ShellContext.getInstance()
                    .getApiDevopsOrganizations()
                    .users().forEach(user -> {
             Map <String, String> rf = new HashMap<>();
             rf.put(COLUMN_USER_ID, user.getUserId());
             rf.put(COLUMN_USER_EMAIL, user.getEmail());
             rf.put(COLUMN_USER_STATUS, user.getStatus().name());
             sht.getCellValues().add(rf);
        });
        ShellPrinter.printShellTable(sht);
        return ExitCode.SUCCESS;
    }
    
    /**
     * Show Role details.
     *
     * @param role
     *      role name
     * @return
     *      exit code
     */
    public static ExitCode showRole(String role) {
        try {
            Optional<Role> optRole = ShellContext
                    .getInstance()
                    .getApiDevopsOrganizations()
                    .findRoleByName(role);
            
            if (!optRole.isPresent() && IdUtils.isUUID(role)) {
                optRole = ShellContext
                        .getInstance()
                        .getApiDevopsOrganizations()
                        .role(role)
                        .find();
            }
            
            if (!optRole.isPresent()) {
                ShellPrinter.outputError(ExitCode.NOT_FOUND, "Role '" + role + "' has not been found.");
                return ExitCode.NOT_FOUND;
            }
            
            Role r = optRole.get();
            ShellTable sht = ShellTable.propertyTable(15, 40);
            sht.addPropertyRow("Identifier",    r.getId());
            sht.addPropertyRow("Name",          r.getName());
            sht.addPropertyRow("Description",   r.getPolicy().getDescription());
            sht.addPropertyRow("Effect",        r.getPolicy().getEffect());
            switch(ShellContext.getInstance().getOutputFormat()) {
                case csv:
                    sht.addPropertyRow("Resources", r.getPolicy().getResources().toString());
                    sht.addPropertyRow("Actions", r.getPolicy().getActions().toString());
                    ShellPrinter.printShellTable(sht);
                break;
                case json:
                    ShellPrinter.printJson(new JsonOutput(ExitCode.SUCCESS, 
                            OperationIam.COMMAND_ROLE + " " + AbstractCmd.GET + " " + role, r));
                break;
                case human:
                default:
                    sht.addPropertyListRows("Resources", r.getPolicy().getResources());
                    sht.addPropertyListRows("Actions",   r.getPolicy().getActions());
                    ShellPrinter.printShellTable(sht);
                break;
            }
            
        } catch(RuntimeException e) {
            ShellPrinter.outputError(ExitCode.INTERNAL_ERROR,"Cannot show role, technical error " + e.getMessage());
            return ExitCode.INTERNAL_ERROR;
        }
        
        return ExitCode.SUCCESS;
    }
    
    /**
     * Show User details.
     *
     * @param user
     *      user email
     * @return
     *      exit code
     */
    public static ExitCode showUser(String user) {
        try {
            Optional<User> optUser = ShellContext
                    .getInstance()
                    .getApiDevopsOrganizations()
                    .findUserByEmail(user);
            
            if (!optUser.isPresent() && IdUtils.isUUID(user)) {
                optUser = ShellContext
                        .getInstance()
                        .getApiDevopsOrganizations()
                        .user(user)
                        .find();
            }
            
            if (!optUser.isPresent()) {
                ShellPrinter.outputError(ExitCode.NOT_FOUND, "User '" + user + "' has not been found.");
                return ExitCode.NOT_FOUND;
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
            
            switch(ShellContext.getInstance().getOutputFormat()) {
                case csv:
                    sht.addPropertyRow("Roles", roleNames.toString());
                    ShellPrinter.printShellTable(sht);
                break;
                case json:
                    ShellPrinter.printJson(new JsonOutput(ExitCode.SUCCESS, "user show " + user, r));
                break;
                case human:
                default:
                    sht.addPropertyListRows("Roles", roleNames);
                    ShellPrinter.printShellTable(sht);
                break;
            }
            
        } catch(RuntimeException e) {
            ShellPrinter.outputError(ExitCode.INTERNAL_ERROR,"Cannot show user, technical error " + e.getMessage());
            return ExitCode.INTERNAL_ERROR;
        }
        
        return ExitCode.SUCCESS;
    }
    
    /**
     * Invite User.
     *
     * @param user
     *      user email
     * @param role
     *      target role for the user
     * @return
     *      exit code
     */
    public static ExitCode inviteUser(String user, String role) {
        try {
            OrganizationsClient oc = ShellContext.getInstance().getApiDevopsOrganizations();
            Optional<User> optUser = oc.findUserByEmail(user);
            
            if (optUser.isPresent()) {
                ShellPrinter.outputWarning(ExitCode.ALREADY_EXIST, "User '" + user + "' already exist in the organization.");
                return ExitCode.ALREADY_EXIST;
            }
            
            Optional<Role> optRole = oc.findRoleByName(role);
            if (!optRole.isPresent() && IdUtils.isUUID(role)) {
                optRole = oc.role(role).find();
            }
            
            if (!optRole.isPresent()) {
                ShellPrinter.outputError(ExitCode.NOT_FOUND, "Role '" + role + "' has not been found");
                return ExitCode.NOT_FOUND;
            }
            
            oc.inviteUser(user, optRole.get().getId());
            
            ShellPrinter.outputSuccess(role);
            
        } catch(RuntimeException e) {
            ShellPrinter.outputError(ExitCode.INTERNAL_ERROR,"Cannot invite user, technical error " + e.getMessage());
            return ExitCode.INTERNAL_ERROR;
        }
        return ExitCode.SUCCESS;          
    }

    /**
     * Delete a user if exist.
     * 
     * @param cmd
     *      current command options
     * @param user
     *      user email of technial identifier
     * @return
     *      status
     */
    public static ExitCode deleteUser(AbstractCmd cmd, String user) {
        try {
            OrganizationsClient oc = ShellContext.getInstance().getApiDevopsOrganizations();
            
            Optional<User> optUser = oc.findUserByEmail(user);
            
            if (!optUser.isPresent() && IdUtils.isUUID(user)) {
                optUser = oc.user(user).find();
            }
            
            if (!optUser.isPresent()) {
                ShellPrinter.outputError(ExitCode.NOT_FOUND, "User '" + user + "' has not been found.");
                return ExitCode.NOT_FOUND;
            }
            
            oc.user(optUser.get().getUserId()).delete();
            ShellPrinter.outputSuccess("Deleting user '" + user + "' (async operation)");
            
        } catch(RuntimeException e) {
            ShellPrinter.outputError(ExitCode.INTERNAL_ERROR,"Cannot delete user, technical error " + e.getMessage());
            return ExitCode.INTERNAL_ERROR;
        }
        
        return ExitCode.SUCCESS;
    }
    
}
