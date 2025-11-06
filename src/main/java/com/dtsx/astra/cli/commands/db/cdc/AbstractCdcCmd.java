package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractCdcCmd<OpRes> extends AbstractPromptForDbCmd<OpRes> {
    protected CdcGateway cdcGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        cdcGateway = ctx.gateways().mkCdcGateway(profile().token(), profile().env());
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to work with:";
    }
}
