package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.token.TokenCreateOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(name = "create", description = "Create a new token")
public final class TokenCreateCmd extends AbstractTokenCmd {
    @Option(
        names = { "-r", "--role" },
        description = "Identifier of the role for this token",
        paramLabel = "ROLE",
        required = true
    )
    private String role;

    @Override
    public OutputAll execute() {
        val operation = new TokenCreateOperation(tokenGateway, roleGateway);
        val tokenResponse = operation.execute(role);
        
        return new ShellTable(List.of(
            ShellTable.attr("Client Id", tokenResponse.getClientId()),
            ShellTable.attr("Client Secret", tokenResponse.getSecret()),
            ShellTable.attr("Token", tokenResponse.getToken())
        )).withAttributeColumns();
    }
}
