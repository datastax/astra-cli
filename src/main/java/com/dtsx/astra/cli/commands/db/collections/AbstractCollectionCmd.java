package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.commands.db.keyspace.AbstractKeyspaceSpecificCmd;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractCollectionCmd<OpRes> extends AbstractKeyspaceSpecificCmd<OpRes> {
    protected CollectionGateway collectionGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        collectionGateway = ctx.gateways().mkCollectionGateway(profile().token(), profile().env());
    }
}
