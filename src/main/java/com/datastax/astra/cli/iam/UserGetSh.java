package com.datastax.astra.cli.iam;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.AbstractInteractiveCmd;
import com.datastax.astra.cli.iam.exception.UserNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Display user.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.GET, description = "Show user details")
public class UserGetSh extends AbstractInteractiveCmd {
    
    /** identifier or email. */
    @Required
    @Arguments(title = "EMAIL", description = "User Email")
    public String user;
    
    /** {@inheritDoc} */
    public void execute() throws UserNotFoundException {
        OperationIam.showUser(user);
    }

}
