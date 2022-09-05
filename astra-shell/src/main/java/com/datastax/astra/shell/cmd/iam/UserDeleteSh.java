package com.datastax.astra.shell.cmd.iam;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.AbstractCmd;
import com.datastax.astra.shell.cmd.BaseSh;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a user if exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.DELETE, description = "Delete an existing user")
public class UserDeleteSh extends BaseSh {
    
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
