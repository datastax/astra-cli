package com.datastax.astra.cli.streaming.exception;

import java.io.Serial;

/**
 * Tenant not found
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class TenantNotFoundException extends RuntimeException {
    
    /** serial. */
    @Serial
    private static final long serialVersionUID = -224037288716231040L;
    
    /**
     * Constructor with tenant name
     * 
     * @param tenantName
     *      tenant name
     */
    public TenantNotFoundException(String tenantName) {
        super("Tenant '" + tenantName + "' has not been found.");
    }

}
