package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.DeviceLoginOperation;
import com.dtsx.astra.cli.operations.DeviceLoginOperation.DeviceLoginRequest;
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
        "Log in to Astra via OAuth2",
        "",
        "By default, opens a browser window to complete authentication (PKCE flow).",
        "Use @|code --device|@ for headless / SSH environments where no browser is available:",
        "a short code is displayed that you enter at the verification URL from any device.",
        "",
        "The resulting token is written to @|code ~/.astrarc|@."
    }
)
@Example(
    comment = "Log in via browser (default)",
    command = "${cli.name} login"
)
@Example(
    comment = "Log in via device code (headless / SSH)",
    command = "${cli.name} login --device"
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
        names = { "--device" },
        description = {
            "Use the RFC 8628 Device Authorization Grant instead of the browser PKCE flow.",
            "Displays a short code to enter at a URL — no local port or browser required.",
            "Ideal for SSH sessions, Docker containers, and headless cloud environments."
        }
    )
    public boolean $device = false;

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
        if ($device) {
            return new DeviceLoginOperation(ctx, new DeviceLoginRequest($loginUrl, $name, !$noDefault));
        }
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
