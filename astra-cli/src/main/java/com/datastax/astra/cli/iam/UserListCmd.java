package com.datastax.astra.cli.iam;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseCmd;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display roles.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.LIST, description = "Display the list of Users in an organization")
public class UserListCmd extends BaseCmd {
   
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.listUsers(this);
    }
    
}
