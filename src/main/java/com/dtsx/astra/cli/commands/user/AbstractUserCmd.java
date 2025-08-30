package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.completions.caches.UserCompletionsCache;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractUserCmd<OpRes> extends AbstractConnectedCmd<OpRes> {
    protected UserGateway userGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        userGateway = ctx.gateways().mkUserGateway(profile().token(), profile().env(), new UserCompletionsCache(ctx, profile().name()), ctx);
    }
}
