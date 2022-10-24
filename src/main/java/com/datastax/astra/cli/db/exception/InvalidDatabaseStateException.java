package com.datastax.astra.cli.db.exception;

import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.sdk.databases.domain.DatabaseStatusType;

import java.io.Serial;

/**
 * Database not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class InvalidDatabaseStateException extends RuntimeException {

    /** Serial Number. */
    @Serial
    private static final long serialVersionUID = -8460056062064740428L;

    /**
     * Simple constructor.
     *
     * @param msg
     *      current message
     */
    public InvalidDatabaseStateException(String msg) {
        super(msg);
    }

    /**
     * Constructor with dbName
     * 
     * @param dbName
     *      database name
     * @param expected
     *      expected status
     * @param current
     *      current db status
     */
    public InvalidDatabaseStateException(String dbName, DatabaseStatusType expected, DatabaseStatusType current) {
        super("Database '" + dbName + "' has been found "
                + "but operation cannot be processed due "
                + "to invalid state (" + (current) + ") expected (" + expected + ")");
    }

}
