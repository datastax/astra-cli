package com.datastax.astra.shell.cmd.iam;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.datastax.astra.shell.cmd.BaseShellCommand;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display roles.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.LIST, description = "Display the list of Users in an organization")
public class UserListShell extends BaseShellCommand {
   
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.listUsers(this);
    }
    
}
