package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.TOKEN_NOT_FOUND;
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

    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if token does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Override
    public final OutputAll execute(TokenDeleteResult result) {
        return switch (result) {
            case TokenDeleted() -> handleTokenDeleted();
            case TokenNotFound() -> handleTokenNotFound();
            case TokenIllegallyNotFound() -> throwTokenNotFound();
        };
    }

    private OutputAll handleTokenDeleted() {
        val message = "Your token has been deleted.";
        val data = mkData(true);
        
        return OutputAll.response(message, data);
    }

    private OutputAll handleTokenNotFound() {
        val message = "Token %s does not exist; nothing to delete.".formatted(highlight(tokenId));
        val data = mkData(false);
        
        return OutputAll.response(message, data, List.of(
            new Hint("See all available tokens:", "astra token list")
        ));
    }

    private <T> T throwTokenNotFound() {
        val originalArgsWithFlag = originalArgs().stream().toList();
        
        throw new AstraCliException(TOKEN_NOT_FOUND, """
          @|bold,red Error: Token '%s' not found.|@

          The specified token does not exist. To avoid this error, pass the @!--if-exists!@ flag to skip this error if the token doesn't exist.
        """.formatted(
            tokenId
        ), List.of(
            new Hint("Example fix:", originalArgsWithFlag, "--if-exists"),
            new Hint("See all available tokens:", "astra token list")
        ));
    }

    private Map<String, Object> mkData(Boolean wasDeleted) {
        return Map.of(
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected Operation<TokenDeleteResult> mkOperation() {
        return new TokenDeleteOperation(tokenGateway, new TokenDeleteRequest(tokenId, ifExists));
    }
}
