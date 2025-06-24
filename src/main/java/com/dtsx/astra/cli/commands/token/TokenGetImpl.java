package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenGetOperation;

public abstract class TokenGetImpl extends AbstractTokenCmd<String> {
    @Override
    public final OutputAll execute(String token) {
        return OutputAll.serializeValue(token);
    }

    @Override
    protected Operation<String> mkOperation() {
        return new TokenGetOperation(profile());
    }
}
