package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenGetOperation;

import java.util.function.Supplier;

public abstract class TokenGetImpl extends AbstractTokenCmd<AstraToken> {
    @Override
    public final OutputAll execute(Supplier<AstraToken> token) {
        return OutputAll.serializeValue(token.get().unwrap());
    }

    @Override
    protected Operation<AstraToken> mkOperation() {
        return new TokenGetOperation(profile());
    }
}
