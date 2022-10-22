package com.datastax.astra.cli.core.exception;

import java.io.Serial;

/**
 * Cannot create or read files on hard disk drive
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class FileSystemException extends RuntimeException {

    /** Serial. */
    @Serial
    private static final long serialVersionUID = -1631087992604077795L;

    /**
     * Constructor with token
     * 
     * @param msg
     *       error message
     */
    public FileSystemException(String msg) {
        super(msg);
    }

    /**
     * Constructor with token
     *
     * @param e
     *      parent exception
     * @param msg
     *       error message
     */
    public FileSystemException(String msg, Exception e) {
        super(msg, e);
    }

}
