package com.datastax.astra.shell.cmd.streaming;

import com.datastax.astra.sdk.streaming.StreamingClient;
import com.datastax.astra.sdk.streaming.domain.CreateTenant;
import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.ShellContext;
import com.datastax.astra.shell.exception.TenantAlreadyExistExcepion;

/**
 * Utility class for command `streaming`
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class OperationsStreaming {

    /** Command constants. */
    public static final String STREAMING = "streaming";
    
    /**
     * default value.
     */
    public static final String DEFAULT_CLOUD_PROVIDER = "aws";
    
    /**
     * default value.
     */
    public static final String DEFAULT_CLOUD_REGION = "useast2";
    
    /**
     * default value.
     */
    public static final String DEFAULT_CLOUD_TENANT = "free";
    
    /**
     * default value.
     */
    public static final String DEFAULT_EMAIL = "astra-cli@datastax.com";
    
    /**
     * Create a streaming tenant.
     *
     * @param ct
     *      tenant creation request
     * @throws TenantAlreadyExistExcepion
     *      already exist exception 
     * @return
     *      returned code.
     */
    public static ExitCode createStreamingTenant(CreateTenant ct) 
    throws TenantAlreadyExistExcepion {
        StreamingClient streamingClient = ShellContext.getInstance().getApiDevopsStreaming();
        
        if (streamingClient.tenant(ct.getPlan()).exist()) {
            throw new TenantAlreadyExistExcepion(ct.getTenantName());
        }
        streamingClient.createTenant(ct);
        return ExitCode.SUCCESS;
    }
    
}
    