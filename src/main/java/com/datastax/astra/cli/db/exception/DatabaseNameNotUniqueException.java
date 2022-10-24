package com.datastax.astra.cli.db.exception;

import com.datastax.astra.cli.core.out.LoggerShell;

import java.io.Serial;

/**
 * Business exception if multiple database names.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class DatabaseNameNotUniqueException extends RuntimeException {

    /** 
     * Serial
     */
    @Serial
    private static final long serialVersionUID = -7880080384291100885L;
   
    /**
     * Constructor with dbName
     * 
     * @param dbName
     *      db name
     */
    public DatabaseNameNotUniqueException(String dbName) {
        super("Cannot create another Database with name '%s' Use flag --if-not-exist to connect to the existing database".formatted(dbName));

    }
    
}
