package com.datastax.astra.shell.cmd.iam;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.datastax.astra.shell.cmd.BaseShellCommand;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display role.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.GET, description = "Show role details")
public class RoleGetShell extends BaseShellCommand {
    
    /** Role name or id. */
    @Required
    @Arguments(title = "ROLE", description = "Role name or identifier")
    public String role;
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.showRole(role);
    }
    
}
