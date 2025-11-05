package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.CliConstants.$Token;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.TOKEN_NOT_FOUND;
import static com.dtsx.astra.cli.operations.token.TokenDeleteOperation.*;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "delete", 
    aliases = { "revoke" },
    description = "Delete a token"
)
@Example(
    comment = "Delete a specific token",
    command = "${cli.name} token delete <client_id>"
)
@Example(
    comment = "Delete a token without failing if it doesn't exist",
    command = "${cli.name} token delete <client_id> --if-exists"
)
public class TokenDeleteCmd extends AbstractTokenCmd<TokenDeleteResult> {
    @Parameters(
        description = "Token identifier",
        paramLabel = $Token.LABEL
    )
    public String $tokenId;

    @Option(
        names = { "--if-exists" },
        description = "Do not fail if token does not exist",
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Override
    public final OutputAll execute(Supplier<TokenDeleteResult> result) {
        return switch (result.get()) {
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
        val message = "Token %s does not exist; nothing to delete.".formatted(ctx.highlight($tokenId));
        val data = mkData(false);
        
        return OutputAll.response(message, data, List.of(
            new Hint("See all available tokens:", "${cli.name} token list")
        ));
    }

    private <T> T throwTokenNotFound() {
        val originalArgsWithFlag = originalArgs().stream().toList();
        
        throw new AstraCliException(TOKEN_NOT_FOUND, """
          @|bold,red Error: Token '%s' not found.|@

          The specified token does not exist. To avoid this error, pass the @'!--if-exists!@ flag to skip this error if the token doesn't exist.
        """.formatted(
            $tokenId
        ), List.of(
            new Hint("Example fix:", originalArgsWithFlag, "--if-exists"),
            new Hint("See all available tokens:", "${cli.name} token list")
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasDeleted) {
        return sequencedMapOf(
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected Operation<TokenDeleteResult> mkOperation() {
        return new TokenDeleteOperation(tokenGateway, new TokenDeleteRequest($tokenId, $ifExists));
    }
}
