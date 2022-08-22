package com.datastax.astra.shell.cmd.iam;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCliCommand;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display role.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.GET, description = "Show role details")
public class RoleGetCli extends BaseCliCommand {
    
    /** Role name or id. */
    @Required
    @Arguments(title = "ROLE", description = "Role name or identifier")
    public String role;
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.showRole(role);
    }
    
}
