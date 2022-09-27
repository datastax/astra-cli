package com.datastax.astra.cli.iam.exception;

import com.datastax.astra.cli.core.out.LoggerShell;

/**
 * Role not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class RoleNotFoundException extends Exception {

    /** Serial Number. */
    private static final long serialVersionUID = -1269813351970244235L;

    /**
     * Default constructor
     */
    public RoleNotFoundException() {}
    
    /**
     * Constructor with roleName
     * 
     * @param roleName
     *      role name
     */
    public RoleNotFoundException(String roleName) {
        super("Role '" + roleName + "' has not been found.");
        LoggerShell.warning(getMessage());
    }

}
