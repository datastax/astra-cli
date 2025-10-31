package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractOrgCmd<OpRes> extends AbstractConnectedCmd<OpRes> {
    protected OrgGateway orgGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        orgGateway = ctx.gateways().mkOrgGateway(profile().token(), profile().env());
    }
}
