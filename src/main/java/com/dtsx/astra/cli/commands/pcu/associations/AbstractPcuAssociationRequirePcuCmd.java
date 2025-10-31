package com.dtsx.astra.cli.commands.pcu.associations;

import com.dtsx.astra.cli.commands.pcu.AbstractPcuRequiredCmd;
import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractPcuAssociationRequirePcuCmd<OpRes> extends AbstractPcuRequiredCmd<OpRes> {
    protected DbGateway dbGateway;
    protected PcuAssociationsGateway associationsGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        dbGateway =  ctx.gateways().mkDbGateway(profile().token(), profile().env(), new DbCompletionsCache(ctx, profile().name()));
        associationsGateway = ctx.gateways().mkPcuAssociationsGateway(profile().token(), profile().env());
    }
}
