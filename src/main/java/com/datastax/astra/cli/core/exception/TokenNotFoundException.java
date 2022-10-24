package com.datastax.astra.cli.core.exception;

import java.io.Serial;

/**
 * Tenant not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class TokenNotFoundException extends RuntimeException {
    
    /** serial. */
    @Serial
    private static final long serialVersionUID = -5461243744804311589L;

    /**
     * Default constructor
     */
    public TokenNotFoundException() {
        super("Token has not been found.");
    }

}
