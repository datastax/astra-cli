package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserInviteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.USER_ALREADY_INVITED;
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
        defaultValue = "Database Administrator",
        required = true
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
        return switch (result) {
            case UserInvited() -> handleUserInvited();
            case UserAlreadyExists() -> handleUserAlreadyExists();
            case UserIllegallyAlreadyExists() -> throwUserAlreadyExists();
        };
    }

    private OutputAll handleUserInvited() {
        val message = "User %s has been invited with role %s.".formatted(highlight(user), highlight(role));

        return OutputAll.response(message, mkData(true));
    }

    private OutputAll handleUserAlreadyExists() {
        val message = "User %s already exists; nothing to invite.".formatted(highlight(user));
        
        return OutputAll.response(message, mkData(false), List.of(
            new Hint("See all existing users:", "astra user list")
        ));
    }

    private <T> T throwUserAlreadyExists() {
        val originalArgsWithFlag = originalArgs().stream().toList();
        
        throw new AstraCliException(USER_ALREADY_INVITED, """
          @|bold,red Error: User '%s' already exists in this organization.|@

          This may be expected, but to avoid this error, pass the @!--if-not-exists!@ flag to skip this error if the user already exists.
        """.formatted(
            user
        ), List.of(
            new Hint("Example fix:", originalArgsWithFlag, "--if-not-exists"),
            new Hint("See all existing users:", "astra user list")
        ));
    }

    private Map<String, Object> mkData(Boolean wasInvited) {
        return Map.of(
            "wasInvited", wasInvited
        );
    }
}
