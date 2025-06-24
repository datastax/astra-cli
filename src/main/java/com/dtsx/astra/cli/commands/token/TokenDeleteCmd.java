package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.token.TokenDeleteOperation.*;

@Command(
    name = "delete", 
    aliases = { "revoke" },
    description = "Delete a token"
)
public class TokenDeleteCmd extends AbstractTokenCmd<TokenDeleteResult> {
    @Parameters(
        index = "0",
        description = "Token identifier",
        paramLabel = "TOKEN"
    )
    private String tokenId;

    @Override
    public final OutputAll execute(TokenDeleteResult result) {
        val message = switch (result) {
            case TokenDeleted() -> "Your token has been deleted.";
            case TokenNotFound() -> throw new TokenNotFoundException(tokenId);
        };
        
        return OutputAll.message(message);
    }

    @Override
    protected Operation<TokenDeleteResult> mkOperation() {
        return new TokenDeleteOperation(tokenGateway, new TokenDeleteRequest(tokenId));
    }

    public static class TokenNotFoundException extends AstraCliException {
        public TokenNotFoundException(String tokenId) {
            super("""
              @|bold,red Error: Token '%s' not found.|@
            
              The specified token does not exist.
            
              Use %s to see all available tokens.
            """.formatted(
                tokenId,
                highlight("astra token list")
            ));
        }
    }
}
