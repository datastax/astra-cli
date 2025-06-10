package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.gateways.keyspace.KeyspaceGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractKeyspaceCmd extends AbstractDbSpecificCmd {
    protected KeyspaceGateway keyspaceGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        keyspaceGateway = KeyspaceGateway.mkDefault(profile().token(), profile().env());
    }
}
