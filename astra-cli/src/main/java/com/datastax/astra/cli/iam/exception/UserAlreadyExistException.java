package com.datastax.astra.cli.iam.exception;

import com.datastax.astra.cli.core.out.LoggerShell;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class UserAlreadyExistException extends Exception {

    /** Serial Number. */
    private static final long serialVersionUID = 968018206118357644L;

    /**
     * Default constructor
     */
    public UserAlreadyExistException() {}
    
    /**
     * Constructor with keyspace name
     * 
     * @param userName
     *      users name
     */
    public UserAlreadyExistException(String userName) {
        super("USer '" + userName + "' already existsin the organization.");
        LoggerShell.warning(getMessage());
    }

}
