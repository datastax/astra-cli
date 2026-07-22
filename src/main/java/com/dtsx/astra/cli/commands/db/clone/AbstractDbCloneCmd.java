package com.dtsx.astra.cli.commands.db.clone;

import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.gateways.db.clone.DbCloneGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractDbCloneCmd<OpRes> extends AbstractPromptForDbCmd<OpRes> {
    protected DbCloneGateway dbCloneGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        dbCloneGateway = ctx.gateways().mkDbCloneGateway(profile().token(), profile().env(), new DbCompletionsCache(ctx, profileAndSource()));

        if (!ctx.properties().disableBetaWarnings()) {
            ctx.log().warn("${cli.name} db clone commands are still in beta and may change without notice.");
        }
    }
}
