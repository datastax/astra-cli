package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.user.UserDeleteOperation.*;

@Command(
    name = "delete",
    description = "Delete an existing user"
)
public class UserDeleteCmd extends AbstractUserCmd<UserDeleteResult> {
    @Parameters(description = "User email or identifier")
    public UserRef user;

    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if user does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Override
    protected Operation<UserDeleteResult> mkOperation() {
        return new UserDeleteOperation(userGateway, new UserDeleteRequest(user, ifExists));
    }

    @Override
    public final OutputAll execute(UserDeleteResult result) {
        val message = switch (result) {
            case UserNotFound() -> "User " + highlight(user) + " does not exist; nothing to delete";
            case UserIllegallyNotFound() -> throw new UserNotFoundException(user);
            case UserDeleted() -> "User " + highlight(user) + " has been deleted (async operation)";
        };
        
        return OutputAll.message(message);
    }

    public static class UserNotFoundException extends AstraCliException {
        public UserNotFoundException(UserRef userRef) {
            super("""
              @|bold,red Error: User '%s' does not exist in this organization.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see all existing users in this organization.
              - Pass the %s flag to skip this error if the user doesn't exist.
            """.formatted(
                userRef,
                highlight("astra user list"),
                highlight("--if-exists")
            ));
        }
    }
}