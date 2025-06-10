package com.dtsx.astra.cli.operations.collection;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.exceptions.db.CollectionNotFoundException;
import com.dtsx.astra.cli.gateways.collection.CollectionGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class CollectionDeleteOperation {
    private final CollectionGateway collectionGateway;

    public record CollectionDeleteRequest(
        CollectionRef collectionRef,
        boolean ifExists
    ) {}

    public sealed interface CollectionDeleteResult {
        record CollectionNotFound(CollectionRef collectionRef) implements CollectionDeleteResult {}
        record CollectionDeleted(CollectionRef collectionRef, CollectionDefinition descriptor) implements CollectionDeleteResult {}
    }

    public CollectionDeleteResult execute(CollectionDeleteRequest request) {
        try {
            val descriptor = collectionGateway.findOneCollection(request.collectionRef);
            collectionGateway.deleteCollection(request.collectionRef);
            return new CollectionDeleteResult.CollectionDeleted(request.collectionRef, descriptor);
        } catch (CollectionNotFoundException e) {
            if (request.ifExists) {
                return new CollectionDeleteResult.CollectionNotFound(request.collectionRef);
            } else {
                throw e;
            }
        }
    }
}
