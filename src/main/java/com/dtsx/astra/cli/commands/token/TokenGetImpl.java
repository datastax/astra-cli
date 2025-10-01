package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenGetOperation;
import com.dtsx.astra.cli.operations.token.TokenGetOperation.CopiedToken;
import com.dtsx.astra.cli.operations.token.TokenGetOperation.GotToken;
import com.dtsx.astra.cli.operations.token.TokenGetOperation.TokenGetRequest;
import com.dtsx.astra.cli.operations.token.TokenGetOperation.TokenGetResponse;
import picocli.CommandLine.Option;

import java.util.function.Supplier;

public abstract class TokenGetImpl extends AbstractTokenCmd<TokenGetResponse> {
    @Option(
        names = { "-c", "--copy" },
        description = "Copy the token to clipboard instead of printing it"
    )
    boolean $copyToClipboard;

    @Option(
        names = { "--validate" },
        description = "Validates the token before returning it"
    )
    boolean $validate;

    @Override
    public final OutputAll execute(Supplier<TokenGetResponse> res) {
        return switch (res.get()) {
            case CopiedToken() -> OutputAll.response("Successfully copied token to clipboard");
            case GotToken(var token) -> OutputAll.serializeValue(token.unsafeUnwrap());
        };
    }

    @Override
    protected Operation<TokenGetResponse> mkOperation() {
        return new TokenGetOperation(ctx, profile(), tokenGateway, new TokenGetRequest(
            $validate,
            $copyToClipboard
        ));
    }
}
