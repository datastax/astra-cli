package com.datastax.astra.shell.exception;

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
     * @param dbName
     *      db name
     */
    public ParamValidationException(String msg) {
        super(msg);
    }

}
