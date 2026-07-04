package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.CliConstants.$Copy;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenGetOperation;
import com.dtsx.astra.cli.operations.token.TokenGetOperation.TokenGetRequest;
import com.dtsx.astra.cli.utils.ShellUtils;
import lombok.val;
import picocli.CommandLine.Option;

import java.util.function.Supplier;

public abstract class TokenGetImpl extends AbstractTokenCmd<AstraToken> {
    @Option(
        names = { $Copy.SHORT, $Copy.LONG },
        description = "Copy the token to clipboard instead of printing it"
    )
    boolean $copyToClipboard;

    @Option(
        names = { "--validate" },
        description = "Validates the token before returning it"
    )
    boolean $validate;

    @Override
    public final OutputAll execute(Supplier<AstraToken> token) {
        val unsafeToken = token.get().unsafeUnwrap();

        if ($copyToClipboard) {
            ShellUtils.copyToClipboard(ctx, unsafeToken);
            return OutputAll.response("Successfully copied token to clipboard");
        }

        return OutputAll.serializeValue(unsafeToken);
    }

    @Override
    protected Operation<AstraToken> mkOperation() {
        return new TokenGetOperation(ctx, profile(), tokenGateway, new TokenGetRequest($validate));
    }
}
