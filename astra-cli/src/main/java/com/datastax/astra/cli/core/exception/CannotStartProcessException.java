package com.datastax.astra.cli.core.exception;

/**
 * Exception throws when third party process cannot start.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class CannotStartProcessException extends Exception {

    /** Serial. */
    private static final long serialVersionUID = 3366100557983747828L;

    /**
     * Default constructor
     */
    public CannotStartProcessException() {}
    
    /**
     * Constructor with token
     * 
     * @param proc
     *      proc token
     */
    public CannotStartProcessException(String proc) {
        super("Cannot start process '" + proc + "' is invalid");
    }
    
    /**
     * Constructor with process
     * 
     * @param proc
     *      invalid token
     * @param parent
     *      parent exception
     */
    public CannotStartProcessException(String proc, Throwable parent) {
        super("Cannot start process '" + proc + "' is invalid: " + parent.getMessage(), parent);
    }
    
}
