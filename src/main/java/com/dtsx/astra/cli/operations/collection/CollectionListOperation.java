package com.dtsx.astra.cli.operations.collection;

import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.collection.CollectionGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CollectionListOperation {
    private final CollectionGateway collectionGateway;

    public record CollectionListResult(
        List<String> collections
    ) {}

    public CollectionListResult execute(KeyspaceRef keyspaceRef) {
        return new CollectionListResult(collectionGateway.findAllCollections(keyspaceRef));
    }
}
