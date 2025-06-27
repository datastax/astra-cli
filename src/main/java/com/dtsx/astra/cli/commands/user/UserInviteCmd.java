package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserInviteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.user.UserInviteOperation.*;

@Command(
    name = "invite",
    description = "Invite a user to an organization"
)
public class UserInviteCmd extends AbstractUserCmd<UserInviteResult> {
    @Parameters(description = "User email")
    public UserRef user;

    @Option(
        names = { "-r", "--role" },
        description = "Role for the user",
        defaultValue = "Database Administrator"
    )
    public String role;

    @Option(
        names = { "--if-not-exists" },
        description = { "Do not fail if user already exists", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifNotExists;

    @Override
    protected Operation<UserInviteResult> mkOperation() {
        return new UserInviteOperation(userGateway, new UserInviteRequest(user, role, ifNotExists));
    }

    @Override
    public final OutputAll execute(UserInviteResult result) {
        val message = switch (result) {
            case UserAlreadyExists() -> "User " + highlight(user) + " already exists; nothing to invite";
            case UserIllegallyAlreadyExists() -> throw new UserAlreadyExistsException(user);
            case UserInvited() -> "User " + highlight(user) + " has been invited with role " + highlight(role);
        };
        
        return OutputAll.message(message);
    }

    public static class UserAlreadyExistsException extends AstraCliException {
        public UserAlreadyExistsException(UserRef userRef) {
            super("""
              @|bold,red Error: User '%s' already exists in this organization.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see all existing users in this organization.
              - Pass the %s flag to skip this error if the user already exists.
            """.formatted(
                userRef,
                highlight("astra user list"),
                highlight("--if-not-exists")
            ));
        }
    }
}