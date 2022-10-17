package com.datastax.astra.cli.iam.exception;

/**
 * User not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class UserNotFoundException extends RuntimeException {

    /** Serial Number. */
    private static final long serialVersionUID = -1134966974107948087L;
    
    /**
     * Constructor with userName
     * 
     * @param userName
     *      user name
     */
    public UserNotFoundException(String userName) {
        super("User " + userName + "' has not been found.");
    }

}
