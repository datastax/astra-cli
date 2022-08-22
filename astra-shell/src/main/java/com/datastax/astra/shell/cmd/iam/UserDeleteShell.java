package com.datastax.astra.shell.cmd.iam;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.datastax.astra.shell.cmd.BaseShellCommand;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a user if exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.DELETE, description = "Delete an existing user")
public class UserDeleteShell extends BaseShellCommand {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "EMAIL", description = "User email or identifier")
    public String user;
    
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.deleteUser(this, user);
    }
    
}
