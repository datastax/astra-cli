package com.datastax.astra.cli.cmd.iam;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.cmd.AbstractCmd;
import com.datastax.astra.cli.cmd.BaseSh;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display roles.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.LIST, description = "Display the list of Roles in an organization")
public class RoleListSh extends BaseSh {
   
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.listRoles();
    }
    
}
