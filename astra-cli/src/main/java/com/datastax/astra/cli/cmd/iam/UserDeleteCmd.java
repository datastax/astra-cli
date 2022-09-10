package com.datastax.astra.cli.cmd.iam;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.cmd.AbstractCmd;
import com.datastax.astra.cli.cmd.BaseCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a user if exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.DELETE, description = "Delete an existing user")
public class UserDeleteCmd extends BaseCmd {
    
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
