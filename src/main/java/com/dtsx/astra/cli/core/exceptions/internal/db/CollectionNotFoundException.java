package com.dtsx.astra.cli.core.exceptions.internal.db;

import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;

public class CollectionNotFoundException extends AstraCliException {
    public CollectionNotFoundException(CollectionRef collectionRef) {
        super("Collection %s not found. Please check that the collection name is correct and that you have access to it.".formatted(collectionRef.highlight()));
    }
}
