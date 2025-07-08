package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenGetOperation;

public abstract class TokenGetImpl extends AbstractTokenCmd<AstraToken> {
    @Override
    public final OutputAll execute(AstraToken token) {
        return OutputAll.serializeValue(token.unwrap());
    }

    @Override
    protected Operation<AstraToken> mkOperation() {
        return new TokenGetOperation(profile());
    }
}
