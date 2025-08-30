package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.completions.caches.TenantCompletionsCache;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractStreamingCmd<OpRes> extends AbstractConnectedCmd<OpRes> {
    protected StreamingGateway streamingGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        streamingGateway = ctx.gateways().mkStreamingGateway(profile().token(), profile().env(), new TenantCompletionsCache(ctx, profile().name()), ctx);
    }
}
