package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.CliConstants.$Token;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.prompters.specific.TokenPrompter;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.EXECUTION_CANCELLED;
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
@Example(
    comment = "Delete a specific token without confirmation prompt",
    command = "${cli.name} token delete --yes"
)
public class TokenDeleteCmd extends AbstractTokenCmd<TokenDeleteResult> {
    @Parameters(
        description = "Token identifier",
        paramLabel = $Token.LABEL,
        arity = "0..1"
    )
    public Optional<String> $tokenId;

    @Option(
        names = { "--if-exists" },
        description = "Do not fail if token does not exist",
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Option(
        names = { "--yes" },
        description = { "Force deletion of token without prompting", SHOW_CUSTOM_DEFAULT + "false if interactively selected" }
    )
    public Optional<Boolean> $forceDelete;

    @Override
    public final OutputAll execute(Supplier<TokenDeleteResult> result) {
        return switch (result.get()) {
            case TokenDeleted(var tokenId) -> handleTokenDeleted(tokenId);
            case TokenNotFound(var tokenId) -> handleTokenNotFound(tokenId);
            case TokenIllegallyNotFound(var tokenId) -> throwTokenNotFound(tokenId);
        };
    }

    private OutputAll handleTokenDeleted(String tokenId) {
        val message = "Token @!%s!@ has been deleted.".formatted(tokenId);
        val data = mkData(true);
        
        return OutputAll.response(message, data);
    }

    private OutputAll handleTokenNotFound(String tokenId) {
        val message = "Token %s does not exist; nothing to delete.".formatted(tokenId);
        val data = mkData(false);
        
        return OutputAll.response(message, data, List.of(
            new Hint("See all available tokens:", "${cli.name} token list")
        ));
    }

    private <T> T throwTokenNotFound(String tokenId) {
        val originalArgsWithFlag = originalArgs().stream().toList();
        
        throw new AstraCliException(TOKEN_NOT_FOUND, """
          @|bold,red Error: Token '%s' not found.|@

          The specified token does not exist. To avoid this error, pass the @'!--if-exists!@ flag to skip this error if the token doesn't exist.
        """.formatted(
            tokenId
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
        return new TokenDeleteOperation(tokenGateway, new TokenDeleteRequest($tokenId, $ifExists, $forceDelete, this::promptForToken, this::assertShouldDelete));
    }

    private String promptForToken() {
        return TokenPrompter.prompt(ctx, tokenGateway, "Select a token to delete", t -> t.fallbackIndex(0).fix(originalArgs(), "<token>"));
    }

    private void assertShouldDelete(String tokenId) {
        val prompt = """
          You are about to permanently delete token @!%s!@.
        
          To confirm, type @'!confirm!@ below or press @!Ctrl+C!@ to cancel.
        """.formatted(tokenId);

        val shouldDelete = ctx.console().prompt(prompt)
            .mapper(Function.identity())
            .requireAnswer()
            .fallbackFlag("--yes")
            .fix(originalArgs(), "--yes")
            .clearAfterSelection()
            .equals("confirm");

        if (!shouldDelete) {
            throw new AstraCliException(EXECUTION_CANCELLED, """
              @|bold,red Error: User input did not match 'confirm'.|@
            
              Token @!%s!@ was not deleted.
            """.formatted(tokenId), List.of(
                new Hint("Skip confirmation prompt:", originalArgs(), "--yes")
            ));
        }
    }
}
