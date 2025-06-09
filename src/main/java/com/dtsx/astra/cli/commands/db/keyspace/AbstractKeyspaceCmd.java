package com.dtsx.astra.cli.commands.db.keyspace;

import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.domain.db.keyspaces.KeyspaceService;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractKeyspaceCmd extends AbstractDbSpecificCmd {
    protected KeyspaceService keyspaceService;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        keyspaceService = KeyspaceService.mkDefault(profile().token(), profile().env());
    }
}
