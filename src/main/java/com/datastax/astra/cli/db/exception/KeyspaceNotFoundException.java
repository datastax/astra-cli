package com.datastax.astra.cli.db.exception;

import com.datastax.astra.cli.core.out.LoggerShell;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class KeyspaceNotFoundException extends Exception {

    /** Serial Number. */
    private static final long serialVersionUID = 8155558354861561721L;
    
    /**
     * Constructor with keyspace name
     * 
     * @param ksName
     *      ks name
     */
    public KeyspaceNotFoundException(String ksName) {
        super("Keyspace '" + ksName + "' has not been found.");
        LoggerShell.warning(getMessage());
    }

}
