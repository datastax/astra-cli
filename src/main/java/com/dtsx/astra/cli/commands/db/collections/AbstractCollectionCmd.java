package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.commands.db.keyspace.AbstractKeyspaceSpecificCmd;
import com.dtsx.astra.cli.domain.db.collections.CollectionService;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractCollectionCmd extends AbstractKeyspaceSpecificCmd {
    protected CollectionService collectionService;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        collectionService = CollectionService.mkDefault(profile().token(), profile().env());
    }
}
