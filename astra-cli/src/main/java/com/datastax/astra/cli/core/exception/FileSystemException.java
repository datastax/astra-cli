package com.datastax.astra.cli.core.exception;

/**
 * Cannot create or read files on hard disk drive
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class FileSystemException extends Exception {

    /** Serial. */
    private static final long serialVersionUID = -1631087992604077795L;

    /**
     * Default constructor
     */
    public FileSystemException() {
    }
    
    /**
     * Constructor with token
     * 
     * @param token
     *      invalid token
     */
    public FileSystemException(String msg) {
        super(msg);
    }    
    
    /**
     * Constructor with token
     * 
     * @param token
     *      invalid token
     * @param parent
     *      parent exception
     */
    public FileSystemException(String msg, Throwable parent) {
        super(msg, parent);
    }

}
