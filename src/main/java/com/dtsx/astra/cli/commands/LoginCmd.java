package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
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

import static com.dtsx.astra.cli.core.output.ExitCode.EXECUTION_CANCELLED;
import static com.dtsx.astra.cli.core.output.ExitCode.ILLEGAL_OPERATION;

@Command(
    name = "login",
    description = {
        "Log in to Astra via OAuth2",
        "",
        "Opens a browser window to complete authentication (PKCE flow).",
        "",
        "The resulting token is written to @|code ~/.astrarc|@."
    }
)
@Example(
    comment = "Log in via browser (default)",
    command = "${cli.name} login"
)
@Example(
    comment = "Log in and save under a named profile",
    command = "${cli.name} login --name my-org"
)
@Example(
    comment = "Log in and select a specific organization",
    command = "${cli.name} login --org my-org-name"
)
@Example(
    comment = "Log in and select a specific role within an organization",
    command = "${cli.name} login --org my-org-name --role Database Administrator"
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
        names = { "--org" },
        description = "Organization name or ID to use. Prompted interactively if omitted and multiple orgs are available. Ignored when the browser login already selected an organization.",
        paramLabel = "ORG"
    )
    public Optional<String> $org = Optional.empty();

    @Option(
        names = { "--role" },
        description = "Role name or ID to use within the selected organization. Prompted interactively if omitted and multiple roles are available. Ignored when the browser login already selected a role.",
        paramLabel = "ROLE"
    )
    public Optional<String> $role = Optional.empty();

    @Option(
        names = { "--keychain", "--keyring" },
        description = "Store credentials in the OS keychain without prompting."
    )
    public boolean $keychain = false;

    @Option(
        names = { "--no-keychain", "--no-keyring" },
        description = "Store credentials in the configuration file (~/.astrarc) instead of the OS keychain."
    )
    public boolean $noKeychain = false;

    @Option(
        names = { "--resource" },
        description = "RFC 8707 resource indicator sent in the authorize and token requests. Defaults to <login base URL>/v1/mcp.",
        paramLabel = "URL",
        hidden = true
    )
    public Optional<String> $resource = Optional.empty();

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
        Optional<Boolean> useKeychain = Optional.empty();
        if ($keychain) {
            useKeychain = Optional.of(true);
        } else if ($noKeychain) {
            useKeychain = Optional.of(false);
        }
        return new LoginOperation(ctx, new LoginRequest($loginUrl, $name, !$noDefault, $org, $role, $resource, useKeychain));
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

    // Thrown (not returned) so that the process exits non-zero; a returned OutputHuman exits 0.
    private OutputHuman handleCancelled() {
        throw new AstraCliException(EXECUTION_CANCELLED, "@|bold,yellow Login timed out or was cancelled.|@ No profile was saved.");
    }

    private OutputHuman handleError(LoginOperation.LoginError e) {
        throw new AstraCliException(ILLEGAL_OPERATION, "@|bold,red Login error:|@ " + e.message());
    }
}
