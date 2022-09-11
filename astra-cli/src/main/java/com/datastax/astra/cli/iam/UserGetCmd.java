package com.datastax.astra.cli.iam;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.BaseCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display user.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.GET, description = "Show user details")
public class UserGetCmd extends BaseCmd {
    
    /** identifier or email. */
    @Required
    @Arguments(title = "EMAIL", description = "User Email")
    public String user;
    
    /** {@inheritDoc} */
    public ExitCode execute() {
        return OperationIam.showUser(user);
    }

}
