package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractKeyspaceCmd<OpRes> extends AbstractDbSpecificCmd<OpRes> {
    protected KeyspaceGateway keyspaceGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        keyspaceGateway = KeyspaceGateway.mkDefault(profile().token(), profile().env());
    }
}
