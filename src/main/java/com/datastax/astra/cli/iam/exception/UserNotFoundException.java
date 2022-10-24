package com.datastax.astra.cli.iam.exception;

import java.io.Serial;

/**
 * User not found
 *
 * @author Cedrick LUNVEN (@clunven)
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
