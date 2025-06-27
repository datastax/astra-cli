package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

import static com.dtsx.astra.cli.operations.token.TokenDeleteOperation.*;

@RequiredArgsConstructor
public class TokenDeleteOperation implements Operation<TokenDeleteResult> {
    private final TokenGateway tokenGateway;
    private final TokenDeleteRequest request;

    public sealed interface TokenDeleteResult {}
    public record TokenDeleted() implements TokenDeleteResult {}
    public record TokenNotFound() implements TokenDeleteResult {}

    public record TokenDeleteRequest(String tokenId) {}

    @Override
    public TokenDeleteResult execute() {
        if (!tokenGateway.exists(request.tokenId)) {
            return new TokenNotFound();
        }
        tokenGateway.delete(request.tokenId);
        return new TokenDeleted();
    }

}
