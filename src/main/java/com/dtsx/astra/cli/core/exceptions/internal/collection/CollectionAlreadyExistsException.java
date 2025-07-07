package com.dtsx.astra.cli.core.exceptions.internal.collection;

import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.CollectionRef;

public class CollectionAlreadyExistsException extends OptionValidationException {
    public CollectionAlreadyExistsException(CollectionRef collectionRef) {
        super("collection", "Collection '%s' already exists. Use --if-not-exists to ignore this error".formatted(collectionRef.name()));
    }
}