package com.datastax.astra.cli.db.exception;

import com.datastax.astra.cli.core.out.LoggerShell;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class KeyspaceAlreadyExistException extends Exception {

    /** Serial Number. */
    private static final long serialVersionUID = 968018206118357644L;

    /**
     * Default constructor
     */
    public KeyspaceAlreadyExistException() {}
    
    /**
     * Constructor with keyspace name
     * 
     * @param ksName
     *      ks name
     * @param dbname
     *      database name
     */
    public KeyspaceAlreadyExistException(String ksName, String dbname) {
        super("Keyspace '" + ksName + "' already exists for database '" + dbname + "'");
        LoggerShell.warning(getMessage());
    }

}
