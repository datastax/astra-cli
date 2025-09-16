package com.dtsx.astra.cli.core.exceptions.internal.streaming.role;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.TenantName;

import static com.dtsx.astra.cli.core.output.ExitCode.TENANT_NOT_FOUND;

public class TenantNotFoundException extends AstraCliException {
    public TenantNotFoundException(TenantName tenant) {
        super(TENANT_NOT_FOUND, """
          @|bold,red Error: Tenant '%s' not found.|@
        
          Use @!${cli.name} streaming list!@ to see all available tenants.
        """.formatted(tenant));
    }
}
