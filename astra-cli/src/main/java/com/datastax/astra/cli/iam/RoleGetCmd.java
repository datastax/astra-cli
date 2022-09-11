package com.datastax.astra.cli.iam;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display role.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.GET, description = "Show role details")
public class RoleGetCmd extends BaseCmd {
    
    /** Role name or id. */
    @Required
    @Arguments(title = "ROLE", description = "Role name or identifier")
    public String role;
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.showRole(role);
    }
    
}
