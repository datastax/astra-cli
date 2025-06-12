package com.dtsx.astra.cli.operations.db.collection;

import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.exceptions.collection.CollectionAlreadyExistsException;
import com.dtsx.astra.cli.core.exceptions.collection.InvalidIndexingOptionsException;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;

@RequiredArgsConstructor
public class CollectionCreateOperation {
    private final CollectionGateway collectionGateway;

    public record CollectionCreateRequest(
        CollectionRef collectionRef,
        Integer dimension,
        String metric,
        String defaultId,
        String embeddingProvider,
        String embeddingModel,
        String embeddingKey,
        List<String> indexingAllow,
        List<String> indexingDeny,
        boolean ifNotExists
    ) {}

    public sealed interface CollectionCreateResult {
        record CollectionAlreadyExists(CollectionRef collectionRef) implements CollectionCreateResult {}
        record CollectionCreated(CollectionRef collectionRef) implements CollectionCreateResult {}
    }

    @SneakyThrows
    public CollectionCreateResult execute(CollectionCreateRequest request) {
        if (request.indexingAllow != null && request.indexingDeny != null) {
            throw new InvalidIndexingOptionsException();
        }

        boolean exists = collectionGateway.collectionExists(request.collectionRef);
        
        if (exists) {
            if (request.ifNotExists) {
                return new CollectionCreateResult.CollectionAlreadyExists(request.collectionRef);
            } else {
                throw new CollectionAlreadyExistsException(request.collectionRef);
            }
        }

        collectionGateway.createCollection(
            request.collectionRef,
            request.dimension,
            request.metric != null ? request.metric : "cosine",
            request.defaultId,
            request.embeddingProvider,
            request.embeddingModel,
            request.embeddingKey,
            request.indexingAllow,
            request.indexingDeny
        );

        return new CollectionCreateResult.CollectionCreated(request.collectionRef);
    }
}
