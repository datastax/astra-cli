package com.dtsx.astra.cli.operations.collection;

import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.gateways.collection.CollectionGateway;
import lombok.RequiredArgsConstructor;

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

    public static class CollectionAlreadyExistsException extends OptionValidationException {
        public CollectionAlreadyExistsException(CollectionRef collectionRef) {
            super("collection", "Collection '%s' already exists. Use --if-not-exists to ignore this error".formatted(collectionRef.name()));
        }
    }

    public static class InvalidIndexingOptionsException extends OptionValidationException {
        public InvalidIndexingOptionsException() {
            super("indexing options", "indexing-allow and indexing-deny are mutually exclusive");
        }
    }

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
