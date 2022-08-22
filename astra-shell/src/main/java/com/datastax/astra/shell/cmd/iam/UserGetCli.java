package com.datastax.astra.shell.cmd.iam;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCliCommand;
import com.datastax.astra.shell.cmd.BaseCommand;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display user.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = BaseCommand.GET, description = "Show user details")
public class UserGetCli extends BaseCliCommand {
    
    /** identifier or email. */
    @Required
    @Arguments(title = "EMAIL", description = "User Email")
    public String user;
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.showUser(user);
    }

}
