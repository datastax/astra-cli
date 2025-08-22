package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.completions.impls.RoleNamesCompletion;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenCreateOperation;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.token.TokenCreateOperation.TokenCreateRequest;

@Command(
    name = "create",
    description = "Create a new token"
)
@Example(
    comment = "Create a token with a specific role",
    command = "${cli.name} token create --role \"Organization Administrator\""
)
public class TokenCreateCmd extends AbstractTokenCmd<CreateTokenResponse> {
    @Option(
        names = { "-r", "--role" },
        description = "The role for this token",
        completionCandidates = RoleNamesCompletion.class,
        paramLabel = "ROLE",
        required = true
    )
    private RoleRef $role;

    @Option(
        names = { "-d", "--description" },
        description = "An optional description for this token",
        paramLabel = "DESCRIPTION"
    )
    private Optional<String> $description;

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
        return new TokenCreateOperation(tokenGateway, new TokenCreateRequest($role, $description));
    }
}
