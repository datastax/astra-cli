package com.datastax.astra.cli.iam.exception;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class UserAlreadyExistException extends Exception {

    /** Serial Number. */
    private static final long serialVersionUID = 968018206118357644L;

    /**
     * Constructor with keyspace name
     * 
     * @param userName
     *      users name
     */
    public UserAlreadyExistException(String userName) {
        super("User '" + userName + "' already existsin the organization.");
    }

}
