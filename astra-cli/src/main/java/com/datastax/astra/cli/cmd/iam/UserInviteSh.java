package com.datastax.astra.cli.cmd.iam;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.cmd.BaseSh;
import com.datastax.astra.sdk.organizations.domain.DefaultRoles;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Invite user.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "invite", description = "Invite a user to an organization")
public class UserInviteSh extends BaseSh {

    /** identifier or email. */
    @Required
    @Arguments(title = "EMAIL", description = "User Email")
    public String user;
    
    /**
     * Cloud provider region to provision
     */
    @Option(name = { "-r", "--role"}, title = "ROLE", arity = 1, 
            description = "Role for the user (default is Database Administrator)")
    protected String role = DefaultRoles.DATABASE_ADMINISTRATOR.getName();
    
    /** {@inheritDoc} */
    @Override
    public ExitCode execute() {
        return OperationIam.inviteUser(user, role);
    }

}
