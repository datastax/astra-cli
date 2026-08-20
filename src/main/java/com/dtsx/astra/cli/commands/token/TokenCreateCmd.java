package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.completions.caches.RoleCompletionsCache;
import com.dtsx.astra.cli.core.completions.impls.RoleNamesCompletion;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.prompters.specific.RoleNamePrompter;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenCreateOperation;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.token.TokenCreateOperation.TokenCreateRequest;

@Command(
    name = "create",
    description = "Create a new token"
)
@Example(
    comment = "Create a token and interactively select your roles",
    command = "${cli.name} token create"
)
@Example(
    comment = "Create a token with a specific role",
    command = "${cli.name} token create --role \"Organization Administrator\""
)
@Example(
    comment = "Create a token with multiple roles",
    command = "${cli.name} token create --role \"RO User,Billing Admin\""
)
public class TokenCreateCmd extends AbstractTokenCmd<CreateTokenResponse> {
    @Option(
        names = { "-r", "--role" },
        description = "List of roles for the token",
        completionCandidates = RoleNamesCompletion.class,
        paramLabel = "ROLE",
        split = ","
    )
    public List<RoleRef> $roles = List.of();

    @Option(
        names = { "-d", "--description" },
        description = "An optional description for this token",
        paramLabel = "DESC"
    )
    public Optional<String> $description;

    @Option(
        names = { "--org" },
        description = "Optional UUID of the organization under which the token will be created. If not provided, the token is created under the organization/enterprise of the authorization token.",
        paramLabel = "ORG"
    )
    public Optional<UUID> $org;

    @Option(
        names = { "-x", "--expiry" },
        description = "Optional expiration date for the token in ISO-8601 format (e.g., 2026-08-19T14:30:00Z). If not provided, the org's max expiry will be used (potentially infinite).",
        paramLabel = "EXPIRY"
    )
    public Optional<Instant> $expiry;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if ($roles.isEmpty()) {
            val gateway = ctx.gateways().mkRoleGateway(profile().token(), profile().env(), new RoleCompletionsCache(ctx));
            $roles = RoleNamePrompter.multiPrompt(ctx, gateway, "Select roles for the new token (does not include all possible roles):", originalArgs());
        }
    }

    @Override
    public final OutputJson executeJson(Supplier<CreateTokenResponse> tokenResponse) {
        return OutputJson.serializeValue(tokenResponse);
    }

    @Override
    public final OutputAll execute(Supplier<CreateTokenResponse> tokenResponse) {
        return ShellTable.forAttributes(new LinkedHashMap<>() {{
            put("Client Id", tokenResponse.get().getClientId());
            put("Client Secret", tokenResponse.get().getSecret());
            put("Token", tokenResponse.get().getToken());
        }});
    }

    @Override
    protected Operation<CreateTokenResponse> mkOperation() {
        val roles = NEList.parse($roles).orElseThrow(() ->
            new AstraCliException(ExitCode.ROLE_NOT_FOUND, "@|bold,red At least one role must be provided to create the token|@")
        );

        return new TokenCreateOperation(tokenGateway, new TokenCreateRequest(
            roles,
            $description,
            $org,
            $expiry
        ));
    }
}
