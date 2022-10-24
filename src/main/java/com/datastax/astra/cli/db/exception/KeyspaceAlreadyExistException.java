package com.datastax.astra.cli.db.exception;

import com.datastax.astra.cli.core.out.LoggerShell;

import java.io.Serial;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class KeyspaceAlreadyExistException extends RuntimeException {

    /** Serial Number. */
    @Serial
    private static final long serialVersionUID = 968018206118357644L;

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
