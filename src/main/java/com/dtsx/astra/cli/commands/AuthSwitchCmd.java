package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.AuthSwitchOperation;
import com.dtsx.astra.cli.operations.AuthSwitchOperation.SwitchRequest;
import com.dtsx.astra.cli.operations.AuthSwitchOperation.SwitchResult;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Command(
    name = "auth-switch",
    description = {
        "Switch the active AstraCS token to a different organization or role.",
        "",
        "Presents a list of previously minted tokens (from all profiles) as well as",
        "a 'Create new token' option.  Selecting an existing token requires no network",
        "call.  Creating a new one re-uses the stored Bearer JWT (no browser login",
        "unless the JWT has expired).",
        "",
        "Use @|code --org|@ / @|code --role|@ to skip the interactive prompt."
    }
)
@Example(
    comment = "Interactively pick an org/role",
    command = "${cli.name} auth-switch"
)
@Example(
    comment = "Switch to a specific org and role (non-interactive)",
    command = "${cli.name} auth-switch --org my-org --role \"Database Administrator\""
)
@Example(
    comment = "Switch for a named profile",
    command = "${cli.name} auth-switch --profile staging --org my-org --role \"Read Only User\""
)
public class AuthSwitchCmd extends AbstractOperationalCmd<SwitchResult> {

    @Option(
        names = { "--org" },
        description = "Organization name or ID to use. Skips the interactive list when provided.",
        paramLabel = "ORG"
    )
    public Optional<String> $org = Optional.empty();

    @Option(
        names = { "--role" },
        description = "Role name or ID within the selected organization. Skips the interactive list when provided.",
        paramLabel = "ROLE"
    )
    public Optional<String> $role = Optional.empty();

    @Option(
        names = { "--profile" },
        description = "Profile to update. Defaults to 'default'.",
        paramLabel = "PROFILE"
    )
    public Optional<ProfileName> $profile = Optional.empty();

    @Override
    protected Operation<SwitchResult> mkOperation() {
        return new AuthSwitchOperation(ctx, new SwitchRequest(Optional.empty(), $profile, $org, $role));
    }

    @Override
    protected OutputHuman executeHuman(Supplier<SwitchResult> result) {
        return switch (result.get()) {
            case AuthSwitchOperation.SwitchSuccess s -> handleSuccess(s);
            case AuthSwitchOperation.SwitchError e  -> handleError(e);
        };
    }

    private OutputHuman handleSuccess(AuthSwitchOperation.SwitchSuccess s) {
        val msg = "@|bold Profile|@ %s @|bold is now using|@ %s @|bold —|@ %s@|bold .|@"
            .formatted(
                ctx.highlight(s.profileName().unwrap()),
                ctx.highlight(s.orgName()),
                ctx.highlight(s.roleName())
            );
        return OutputHuman.response(msg, List.of(
            new com.dtsx.astra.cli.core.output.Hint("Try it out!", "${cli.name} db list")
        ));
    }

    private OutputHuman handleError(AuthSwitchOperation.SwitchError e) {
        return OutputHuman.response("@|bold,red Error:|@ " + e.message());
    }
}
