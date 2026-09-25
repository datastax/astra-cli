package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.LoginOperation;
import com.dtsx.astra.cli.operations.LoginOperation.LoginRequest;
import com.dtsx.astra.cli.operations.LoginOperation.LoginResult;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Command(
    name = "login",
    description = {
        "Log in to Astra via the browser using OAuth2",
        "",
        "Opens a browser window to complete authentication, then writes the resulting",
        "token to @|code ~/.astrarc|@. The local callback server listens on a randomly-chosen",
        "port and is torn down immediately after the token is received."
    }
)
@Example(
    comment = "Log in and save as the default profile",
    command = "${cli.name} login"
)
@Example(
    comment = "Log in and save under a named profile",
    command = "${cli.name} login --name my-org"
)
public class LoginCmd extends AbstractOperationalCmd<LoginResult> {
    @Option(
        names = { "--name" },
        description = "Profile name to create or overwrite. Defaults to 'default'.",
        paramLabel = "NAME"
    )
    public Optional<ProfileName> $name = Optional.empty();

    @Option(
        names = { "--no-default" },
        description = "Do not set this profile as the default profile."
    )
    public boolean $noDefault = false;

    @Option(
        names = { "--login-url" },
        description = "Override the Astra login URL (for internal/testing use).",
        paramLabel = "URL",
        hidden = true
    )
    public Optional<String> $loginUrl = Optional.empty();

    @Override
    protected OutputHuman executeHuman(Supplier<LoginResult> result) {
        return switch (result.get()) {
            case LoginOperation.LoginSuccess s -> handleSuccess(s);
            case LoginOperation.LoginCancelled _ -> handleCancelled();
            case LoginOperation.LoginError e -> handleError(e);
        };
    }

    @Override
    protected Operation<LoginResult> mkOperation() {
        return new LoginOperation(ctx, new LoginRequest($loginUrl, $name, !$noDefault));
    }

    private OutputHuman handleSuccess(LoginOperation.LoginSuccess s) {
        val msg = s.isDefault()
            ? "@|bold Profile|@ %s @|bold successfully created and set as default.|@"
                  .formatted(ctx.highlight(s.profileName().unwrap()))
            : "@|bold Profile|@ %s @|bold successfully created.|@"
                  .formatted(ctx.highlight(s.profileName().unwrap()));

        return OutputHuman.response(msg, List.of(
            new com.dtsx.astra.cli.core.output.Hint("Try it out!", "${cli.name} db list"),
            new com.dtsx.astra.cli.core.output.Hint("Manage your profiles", "${cli.name} config list")
        ));
    }

    private OutputHuman handleCancelled() {
        return OutputHuman.response("@|bold,yellow Login timed out or was cancelled.|@ No profile was saved.");
    }

    private OutputHuman handleError(LoginOperation.LoginError e) {
        return OutputHuman.response("@|bold,red Login error:|@ " + e.message());
    }
}
