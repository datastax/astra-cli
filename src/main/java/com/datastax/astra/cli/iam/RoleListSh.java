package com.datastax.astra.cli.iam;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.AbstractInteractiveCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display roles.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.LIST, description = "Display the list of Roles in an organization")
public class RoleListSh extends AbstractInteractiveCmd {
   
    /** {@inheritDoc} */
    public void execute() {
        OperationIam.listRoles();
    }
    
}
