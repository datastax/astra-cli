package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.completions.impls.UserEmailsCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.USER_NOT_FOUND;
import static com.dtsx.astra.cli.operations.user.UserDeleteOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "delete",
    description = "Delete an existing user"
)
@Example(
    comment = "Delete a specific user",
    command = "${cli.name} user delete john@example.com"
)
@Example(
    comment = "Delete a user without failing if they doesn't exist",
    command = "${cli.name} user delete john@example.com --if-exists"
)
public class UserDeleteCmd extends AbstractUserCmd<UserDeleteResult> {
    @Parameters(
        description = "User email/id to delete",
        paramLabel = "USER",
        completionCandidates = UserEmailsCompletion.class
    )
    public UserRef $user;

    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if user does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Override
    protected Operation<UserDeleteResult> mkOperation() {
        return new UserDeleteOperation(userGateway, new UserDeleteRequest($user, $ifExists));
    }

    @Override
    public final OutputAll execute(Supplier<UserDeleteResult> result) {
        return switch (result.get()) {
            case UserDeleted() -> handleUserDeleted();
            case UserNotFound() -> handleUserNotFound();
            case UserIllegallyNotFound() -> throwUserNotFound();
        };
    }

    private OutputAll handleUserDeleted() {
        val message = "User %s has been deleted (async operation).".formatted(ctx.highlight($user));

        return OutputAll.response(message, mkData(true));
    }

    private OutputAll handleUserNotFound() {
        val message = "User %s does not exist; nothing to delete.".formatted(ctx.highlight($user));
        
        return OutputAll.response(message, mkData(false), List.of(
            new Hint("See all existing users:", "${cli.name} user list")
        ));
    }

    private <T> T throwUserNotFound() {
        throw new AstraCliException(USER_NOT_FOUND, """
          @|bold,red Error: User '%s' does not exist in this organization.|@

          This may be expected, but to avoid this error, pass the @!--if-exists!@ flag to skip this error if the user doesn't exist.
        """.formatted(
            $user
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("See all existing users:", "${cli.name} user list")
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasDeleted) {
        return sequencedMapOf(
            "wasDeleted", wasDeleted
        );
    }
}
