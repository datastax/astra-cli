package com.datastax.astra.cli.iam;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseSh;
import com.datastax.astra.cli.iam.exception.RoleNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display role.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.GET, description = "Show role details")
public class RoleGetSh extends BaseSh {
    
    /** Role name or id. */
    @Required
    @Arguments(title = "ROLE", description = "Role name or identifier")
    public String role;
    
    /** {@inheritDoc} */
    public void execute() throws RoleNotFoundException {
        OperationIam.showRole(role);
    }
    
}
