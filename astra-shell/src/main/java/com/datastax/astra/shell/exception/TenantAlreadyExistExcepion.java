package com.datastax.astra.shell.exception;

/**
 * Tenant already existing exception
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class TenantAlreadyExistExcepion extends Exception {

    /**
     * Serial number.
     */
    private static final long serialVersionUID = 2456952914005268575L;

    /**
     * Default constructor
     */
    public TenantAlreadyExistExcepion() {}
    
    /**
     * Constructor with dbName
     * 
     * @param tenantName
     *      tenant name
     */
    public TenantAlreadyExistExcepion(String tenantName) {
        super("Tenant name '" + tenantName + "' already exist and must be unique for the cluster.");
    }
    
}
