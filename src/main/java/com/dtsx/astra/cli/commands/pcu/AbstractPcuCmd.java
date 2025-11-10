package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.completions.caches.PcuGroupsCompletionsCache;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractPcuCmd<OpRes> extends AbstractConnectedCmd<OpRes> {
    protected PcuGateway pcuGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        if (!ctx.properties().disableBetaWarnings()) {
            ctx.log().warn("PCU operations are still in beta and may change without notice.");
        }
        pcuGateway = ctx.gateways().mkPcuGateway(profile().token(), profile().env(), new PcuGroupsCompletionsCache(ctx, profileAndSource()));
    }
}
