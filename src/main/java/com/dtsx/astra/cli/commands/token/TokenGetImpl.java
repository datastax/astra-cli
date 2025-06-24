package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.token.TokenGetOperation;
import lombok.val;

public abstract class TokenGetImpl extends AbstractTokenCmd {
    @Override
    public OutputAll execute() {
        val operation = new TokenGetOperation(profile());
        val token = operation.execute();
        return OutputAll.serializeValue(token);
    }
}
