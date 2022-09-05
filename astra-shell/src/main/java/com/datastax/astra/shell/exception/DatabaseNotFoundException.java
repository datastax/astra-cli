package com.datastax.astra.shell.exception;

import com.datastax.astra.shell.out.LoggerShell;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabaseNotFoundException extends Exception {

    /** Serial Number. */
    private static final long serialVersionUID = 8155558354861561721L;
    
    /**
     * Default constructor
     */
    public DatabaseNotFoundException() {}
    
    /**
     * Constructor with dbName
     * 
     * @param dbName
     *      db name
     */
    public DatabaseNotFoundException(String dbName) {
        super("Database '" + dbName + "' has not been found.");
        LoggerShell.warning(getMessage());
    }

}
