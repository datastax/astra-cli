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
        super(("Keyspace '%s' already exists for database %s. " +
                "Cannot create another keyspace with same name. " +
                "Use flag --if-not-exist to connect to the existing keyspace.").formatted(ksName, dbname));
    }

}
