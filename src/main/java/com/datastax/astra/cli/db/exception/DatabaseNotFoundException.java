package com.datastax.astra.cli.db.exception;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabaseNotFoundException extends RuntimeException {

    /** Serial Number. */
    private static final long serialVersionUID = 8155558354861561721L;
    
    /**
     * Constructor with dbName
     * 
     * @param dbName
     *      db name
     */
    public DatabaseNotFoundException(String dbName) {
        super("Database '" + dbName + "' has not been found.");
    }

}
