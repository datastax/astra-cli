package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenGetOperation;

public abstract class TokenGetImpl extends AbstractTokenCmd<Token> {
    @Override
    public final OutputAll execute(Token token) {
        return OutputAll.serializeValue(token);
    }

    @Override
    protected Operation<Token> mkOperation() {
        return new TokenGetOperation(profile());
    }
}
