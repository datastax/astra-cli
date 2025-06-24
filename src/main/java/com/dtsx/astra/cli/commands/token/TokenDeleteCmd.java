package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.token.TokenDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
    name = "delete", 
    aliases = { "revoke" },
    description = "Delete a token"
)
public final class TokenDeleteCmd extends AbstractTokenCmd {
    @Parameters(
        index = "0",
        description = "Token identifier",
        paramLabel = "TOKEN"
    )
    private String tokenId;

    @Override
    public OutputAll execute() {
        val operation = new TokenDeleteOperation(tokenGateway);
        operation.execute(tokenId);
        return OutputAll.message("Your token has been deleted.");
    }
}
