package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.commands.db.keyspace.AbstractKeyspaceSpecificCmd;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractCollectionCmd extends AbstractKeyspaceSpecificCmd {
    protected CollectionGateway collectionGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        collectionGateway = CollectionGateway.mkDefault(profile().token(), profile().env());
    }
}
