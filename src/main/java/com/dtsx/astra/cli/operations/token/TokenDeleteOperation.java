package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.token.TokenDeleteOperation.TokenDeleteResult;

@RequiredArgsConstructor
public class TokenDeleteOperation implements Operation<TokenDeleteResult> {
    private final TokenGateway tokenGateway;
    private final TokenDeleteRequest request;

    public sealed interface TokenDeleteResult {}
    public record TokenDeleted(String tokenId) implements TokenDeleteResult {}
    public record TokenNotFound(String tokenId) implements TokenDeleteResult {}
    public record TokenIllegallyNotFound(String tokenId) implements TokenDeleteResult {}

    public record TokenDeleteRequest(
        Optional<String> tokenId,
        boolean ifExists,
        Optional<Boolean> forceDelete,
        Supplier<String> promptForToken,
        Consumer<String> assertShouldDelete
    ) {}

    @Override
    public TokenDeleteResult execute() {
        val tokenId = request.tokenId.orElseGet(request.promptForToken);

        // for backwards compatability, if tokenId is present, we force delete without prompting
        // otherwise if it's interactively selected we can prompt before deleting since it was added in the same update
        val forceDelete = request.forceDelete.orElse(request.tokenId.isPresent());

        if (!forceDelete) {
            request.assertShouldDelete.accept(tokenId);
        }

        val status = tokenGateway.delete(tokenId);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> new TokenDeleted(tokenId);
            case DeletionStatus.NotFound<?> _ -> handleTokenNotFound(tokenId, request.ifExists);
        };
    }

    private TokenDeleteResult handleTokenNotFound(String tokenId, boolean ifExists) {
        if (ifExists) {
            return new TokenNotFound(tokenId);
        } else {
            return new TokenIllegallyNotFound(tokenId);
        }
    }
}
