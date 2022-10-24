package com.datastax.astra.cli.iam.exception;

import java.io.Serial;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class UserAlreadyExistException extends RuntimeException {

    /** Serial Number. */
    @Serial
    private static final long serialVersionUID = 968018206118357644L;

    /**
     * Constructor with keyspace name
     * 
     * @param userName
     *      users name
     */
    public UserAlreadyExistException(String userName) {
        super("User '" + userName + "' already exists in the organization.");
    }

}
