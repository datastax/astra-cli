package com.datastax.astra.shell.cmd.iam;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCliCommand;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.github.rvesse.airline.annotations.Command;

/**
 * Display roles.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.LIST, description = "Display the list of Roles in an organization")
public class RoleListCli extends BaseCliCommand {
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.listRoles();
    }
    
}
