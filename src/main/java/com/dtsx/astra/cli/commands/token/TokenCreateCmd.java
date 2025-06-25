package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenCreateOperation;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

import static com.dtsx.astra.cli.operations.token.TokenCreateOperation.*;

@Command(
    name = "create"
)
public class TokenCreateCmd extends AbstractTokenCmd<CreateTokenResponse> {
    @Option(
        names = { "-r", "--role" },
        description = "Identifier of the role for this token",
        paramLabel = "ROLE",
        required = true
    )
    private String role;

    @Override
    public final OutputAll execute(CreateTokenResponse tokenResponse) {
        return new ShellTable(List.of(
            ShellTable.attr("Client Id", tokenResponse.getClientId()),
            ShellTable.attr("Client Secret", tokenResponse.getSecret()),
            ShellTable.attr("Token", tokenResponse.getToken())
        )).withAttributeColumns();
    }

    @Override
    protected Operation<CreateTokenResponse> mkOperation() {
        return new TokenCreateOperation(tokenGateway, roleGateway, new TokenCreateRequest(role));
    }
}
