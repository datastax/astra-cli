package com.datastax.astra.shell.cmd.iam;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCmd;
import com.datastax.astra.shell.cmd.AbstractCmd;
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
