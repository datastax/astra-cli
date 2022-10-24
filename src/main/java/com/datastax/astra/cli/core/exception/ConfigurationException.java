package com.datastax.astra.cli.core.exception;

import java.io.Serial;

/**
 * Tenant not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class ConfigurationException extends RuntimeException {
    
    /** serial. */
    @Serial
    private static final long serialVersionUID = 134379114903653046L;

    /**
     * Configuration error.
     * 
     * @param msg
     *      errors
     */
    public ConfigurationException(String msg) {
        super(msg);
    }

}
