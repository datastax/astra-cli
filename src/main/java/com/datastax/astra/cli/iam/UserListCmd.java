package com.datastax.astra.cli.iam;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display roles.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "list", description = "Display the list of Users in an organization")
public class UserListCmd extends AbstractConnectedCmd {
   
    /** {@inheritDoc} */
    public void execute() {
        OperationIam.listUsers(this);
    }
    
}
