package com.datastax.astra.shell.exception;

import com.datastax.astra.shell.out.LoggerShell;

/**
 * Business exception if multiple database names.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabaseNameNotUniqueException extends Exception {

    /** 
     * Serial
     */
    private static final long serialVersionUID = -7880080384291100885L;
    
    /**
     * Default constructor
     */
    public DatabaseNameNotUniqueException() {}
    
    /**
     * Constructor with dbName
     * 
     * @param dbName
     *      db name
     */
    public DatabaseNameNotUniqueException(String dbName) {
        super("Multiple databases with name '" + dbName + "' exist.");
        LoggerShell.warning(getMessage());
    }
    
}
