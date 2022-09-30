package com.datastax.astra.cli.core.exception;

/**
 * Tenant not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class ConfigurationException extends Exception {
    
    /** serial. */
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
