package com.datastax.astra.cli.core.exception;

import java.io.Serial;

/**
 * Invalid parameter provided.
 *
 * @author Cedrick LUNVEN (@clunven)
 *
 */
public class InvalidArgumentException extends RuntimeException {

    /**
     * Serial
     */
    @Serial
    private static final long serialVersionUID = -7880080384291100885L;

    /**
     * Default constructor
     */
    public InvalidArgumentException() {}

    /**
     * Constructor with dbName
     *
     * @param msg
     *      error message
     */
    public InvalidArgumentException(String msg) {
        super(msg);
    }

}