package com.datastax.astra.cli.db.exception;

import com.datastax.astra.cli.core.out.LoggerShell;

/**
 * Business exception if multiple database names.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabaseNameNotUniqueException extends RuntimeException {

    /** 
     * Serial
     */
    private static final long serialVersionUID = -7880080384291100885L;
   
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
