package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractTokenCmd<OpRes> extends AbstractConnectedCmd<OpRes> {
    protected TokenGateway tokenGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        tokenGateway = ctx.gateways().mkTokenGateway(profile().token(), profile().env());
    }
}
