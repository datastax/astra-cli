package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractDbCmd<OpRes> extends AbstractConnectedCmd<OpRes> {
    protected DbGateway dbGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        dbGateway = ctx.gateways().mkDbGateway(profile().token(), profile().env(), new DbCompletionsCache(ctx, profile().name()), ctx);
    }
}
