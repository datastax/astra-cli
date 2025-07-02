package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import static com.dtsx.astra.cli.operations.token.TokenDeleteOperation.*;

@RequiredArgsConstructor
public class TokenDeleteOperation implements Operation<TokenDeleteResult> {
    private final TokenGateway tokenGateway;
    private final TokenDeleteRequest request;

    public sealed interface TokenDeleteResult {}
    public record TokenDeleted() implements TokenDeleteResult {}
    public record TokenNotFound() implements TokenDeleteResult {}
    public record TokenIllegallyNotFound() implements TokenDeleteResult {}

    public record TokenDeleteRequest(String tokenId, boolean ifExists) {}

    @Override
    public TokenDeleteResult execute() {
        val status = tokenGateway.delete(request.tokenId);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> new TokenDeleted();
            case DeletionStatus.NotFound<?> _ -> handleTokenNotFound(request.ifExists);
        };
    }

    private TokenDeleteResult handleTokenNotFound(boolean ifExists) {
        if (ifExists) {
            return new TokenNotFound();
        } else {
            return new TokenIllegallyNotFound();
        }
    }

}
