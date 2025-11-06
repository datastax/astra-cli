package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.commands.db.keyspace.AbstractPromptForKeyspaceCmd;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractCollectionCmd<OpRes> extends AbstractPromptForKeyspaceCmd<OpRes> {
    protected CollectionGateway collectionGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        collectionGateway = ctx.gateways().mkCollectionGateway(profile().token(), profile().env());
    }
}
