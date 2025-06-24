package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenDeleteOperation {
    private final TokenGateway tokenGateway;

    public void execute(String tokenId) {
        if (!tokenGateway.exists(tokenId)) {
            throw new TokenNotFoundException(tokenId);
        }
        tokenGateway.delete(tokenId);
    }

    public static class TokenNotFoundException extends AstraCliException {
        public TokenNotFoundException(String tokenId) {
            super("""
              @|bold,red Error: Token '%s' not found.|@
            
              The specified token does not exist.
            
              Use %s to see all available tokens.
            """.formatted(
                tokenId,
                AstraColors.highlight("astra token list")
            ));
        }
    }
}