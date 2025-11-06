package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractKeyspaceCmd<OpRes> extends AbstractPromptForDbCmd<OpRes> {
    protected KeyspaceGateway keyspaceGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        keyspaceGateway = ctx.gateways().mkKeyspaceGateway(profile().token(), profile().env());
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to work with";
    }
}
