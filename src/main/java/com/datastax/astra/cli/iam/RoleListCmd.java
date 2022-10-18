package com.datastax.astra.cli.iam;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display roles.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "list", description = "Display the list of Roles in an organization")
public class RoleListCmd extends AbstractConnectedCmd {
    
    /** {@inheritDoc} */
    public void execute() {
        OperationIam.listRoles();
    }
    
}
