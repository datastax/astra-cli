package com.datastax.astra.cli.streaming.exception;

/**
 * Tenant already existing exception
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class TenantAlreadyExistException extends RuntimeException {

    /**
     * Constructor with dbName
     * 
     * @param tenantName
     *      tenant name
     */
    public TenantAlreadyExistException(String tenantName) {
        super("Tenant name '%s' already exist and must be unique for the cluster.".formatted(tenantName));
    }
    
}
