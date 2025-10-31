package com.dtsx.astra.cli.commands.pcu.associations;

import com.dtsx.astra.cli.commands.pcu.AbstractPromptForPcuCmd;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractPcuAssociationPromptForPcuCmd<OpRes> extends AbstractPromptForPcuCmd<OpRes> {
    protected PcuAssociationsGateway associationsGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        associationsGateway = ctx.gateways().mkPcuAssociationsGateway(profile().token(), profile().env(), pcuGateway);
    }
}
