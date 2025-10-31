package com.dtsx.astra.cli.commands.pcu.associations;

import com.dtsx.astra.cli.commands.pcu.AbstractPcuCmd;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractPcuAssociationCmd<OpRes> extends AbstractPcuCmd<OpRes> {
    protected PcuAssociationsGateway associationsGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        associationsGateway = ctx.gateways().mkPcuAssociationsGateway(profile().token(), profile().env(), pcuGateway);
    }
}
