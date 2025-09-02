package com.dtsx.astra.cli.core.exceptions.internal.role;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.RoleRef;

import static com.dtsx.astra.cli.core.output.ExitCode.ROLE_NOT_FOUND;

public class RoleNotFoundException extends AstraCliException {
    public RoleNotFoundException(RoleRef role) {
        super(ROLE_NOT_FOUND, """
          @|bold,red Error: Role '%s' not found.|@
        
          The specified role does not exist.
        
          Use @!${cli.name} role list!@ to see all available roles.
        """.formatted(role));
    }
}
