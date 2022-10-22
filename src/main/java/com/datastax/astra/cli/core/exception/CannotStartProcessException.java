package com.datastax.astra.cli.core.exception;

import java.io.Serial;

/**
 * Exception throws when third party process cannot start.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class CannotStartProcessException extends RuntimeException {

    /** Serial. */
    @Serial
    private static final long serialVersionUID = 3366100557983747828L;

    /**
     * Constructor with process
     * 
     * @param process
     *      process name
     * @param parent
     *      parent exception
     */
    public CannotStartProcessException(String process, Throwable parent) {
        super("Cannot start process '%s', error:%s".formatted(process, parent.getMessage()), parent);
    }
    
}
