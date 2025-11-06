package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.commands.db.keyspace.AbstractPromptForKeyspaceCmd;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractTableCmd<OpRes> extends AbstractPromptForKeyspaceCmd<OpRes> {
    protected TableGateway tableGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        tableGateway = ctx.gateways().mkTableGateway(profile().token(), profile().env());
    }
}
