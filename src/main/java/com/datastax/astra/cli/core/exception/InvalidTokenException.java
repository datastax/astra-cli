package com.datastax.astra.cli.core.exception;

/**
 * Invalid parameter provided.
 *
 * @author Cedrick LUNVEN (@clunven)
 *
 */
public class InvalidTokenException extends RuntimeException {

    /** 
     * Serial
     */
    private static final long serialVersionUID = -7071867151543923236L;

    /**
     * Default constructor
     */
    public InvalidTokenException() {}
    
    /**
     * Constructor with token
     * 
     * @param token
     *      invalid token
     */
    public InvalidTokenException(String token) {
        super("Token '" + token + "' is invalid");
    }
    
    /**
     * Constructor with token
     * 
     * @param token
     *      invalid token
     * @param parent
     *      parent exception
     */
    public InvalidTokenException(String token, Throwable parent) {
        super("Token '" + token + "' is invalid: " + parent.getMessage(), parent);
    }

}
