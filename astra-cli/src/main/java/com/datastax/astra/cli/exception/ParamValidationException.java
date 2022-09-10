package com.datastax.astra.cli.exception;

/**
 * Invalid parameter provided.
 *
 * @author Cedrick LUNVEN (@clunven)
 *
 */
public class ParamValidationException extends Exception {

    /** 
     * Serial
     */
    private static final long serialVersionUID = -7880080384291100885L;
    
    /**
     * Default constructor
     */
    public ParamValidationException() {}
    
    /**
     * Constructor with dbName
     * 
     * @param msg
     *      error message
     */
    public ParamValidationException(String msg) {
        super(msg);
    }

}
