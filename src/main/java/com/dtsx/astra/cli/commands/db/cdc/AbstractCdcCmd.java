package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractCdcCmd extends AbstractDbSpecificCmd {
    protected CdcGateway cdcGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        cdcGateway = CdcGateway.mkDefault(profile().token(), profile().env());
    }
}