package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.core.completions.impls.UserEmailsCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.user.UserInviteOperation;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.output.ExitCode.USER_ALREADY_INVITED;
import static com.dtsx.astra.cli.operations.user.UserInviteOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Example(
    comment = "Invite a user with default \"Database Administrator\" role",
    command = "${cli.name} user invite john@example.com"
)
@Example(
    comment = "Invite a user with specific roles",
    command = "${cli.name} user invite john@example.com --role 'R/W User' --role 'Billing Admin'"
)
@Example(
    comment = "Invite a user without failing if they already exist",
    command = "${cli.name} user invite john@example.com --if-not-exists"
)
@Command(
    name = "invite",
    description = "Invite a user to an organization"
)
public class UserInviteCmd extends AbstractUserCmd<UserInviteResult> {
    @Parameters(
        description = "User email/id to invite",
        paramLabel = "USER",
        completionCandidates = UserEmailsCompletion.class
    )
    public UserRef $user;

    @Option(
        names = { "-r", "--roles" },
        description = { "List of roles to assign the user", DEFAULT_VALUE },
        defaultValue = "Database Administrator",
        split = ","
    )
    public List<RoleRef> $roles;

    @Option(
        names = { "--if-not-exists" },
        description = { "Do not fail if user already exists", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifNotExists;

    @Override
    protected void prelude() {
        super.prelude();

        if ($roles.isEmpty()) {
            throw new ParameterException(spec.commandLine(), "At least one role must be specified for the user via the --roles option.");
        }
    }

    @Override
    protected Operation<UserInviteResult> mkOperation() {
        return new UserInviteOperation(userGateway, new UserInviteRequest($user, $roles, $ifNotExists));
    }

    @Override
    public final OutputAll execute(Supplier<UserInviteResult> result) {
        return switch (result.get()) {
            case UserInvited(var roleIds) -> handleUserInvited(roleIds);
            case UserAlreadyExists() -> handleUserAlreadyExists();
            case UserIllegallyAlreadyExists() -> throwUserAlreadyExists();
        };
    }

    private OutputAll handleUserInvited(List<UUID> roleIds) {
        val message = ($roles.size() == 1)
            ? "User %s has been invited with role %s.".formatted(ctx.highlight($user), ctx.highlight($roles.getFirst()))
            : "User %s has been invited with roles %s.".formatted(ctx.highlight($user), $roles.stream().map(r -> r.highlight(ctx)).collect(Collectors.joining(", ")));

        return OutputAll.response(message, mkData(true, roleIds));
    }

    private OutputAll handleUserAlreadyExists() {
        val message = "User %s already exists; nothing to invite.".formatted(ctx.highlight($user));
        
        return OutputAll.response(message, mkData(false, null), List.of(
            new Hint("See all existing users:", "${cli.name} user list")
        ));
    }

    private <T> T throwUserAlreadyExists() {
        val originalArgsWithFlag = originalArgs().stream().toList();
        
        throw new AstraCliException(USER_ALREADY_INVITED, """
          @|bold,red Error: User '%s' already exists in this organization.|@

          This may be expected, but to avoid this error, pass the @'!--if-not-exists!@ flag to skip this error if the user already exists.
        """.formatted(
            $user
        ), List.of(
            new Hint("Example fix:", originalArgsWithFlag, "--if-not-exists"),
            new Hint("See all existing users:", "${cli.name} user list")
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasInvited, @Nullable List<UUID> roleIds) {
        return sequencedMapOf(
            "wasInvited", wasInvited,
            "roleIds", Optional.ofNullable(roleIds)
        );
    }
}
