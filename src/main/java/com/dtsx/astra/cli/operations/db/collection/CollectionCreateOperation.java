package com.dtsx.astra.cli.operations.db.collection;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation.CollectionCreateResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor
public class CollectionCreateOperation implements Operation<CollectionCreateResult> {
    private final CollectionGateway collectionGateway;
    private final CollectionCreateRequest request;

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

    public sealed interface CollectionCreateResult {}
    public record CollectionAlreadyExists() implements CollectionCreateResult {}
    public record CollectionIllegallyAlreadyExists() implements CollectionCreateResult {}
    public record CollectionCreated() implements CollectionCreateResult {}

    @Override
    public CollectionCreateResult execute() {
        val status = collectionGateway.createCollection(
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

        return switch (status) {
            case CreationStatus.Created<?> _ -> handleCollCreated();
            case CreationStatus.AlreadyExists<?> _ -> handleCollAlreadyExists(request.collectionRef, request.ifNotExists);
        };
    }

    private CollectionCreateResult handleCollCreated() {
        return new CollectionCreated();
    }

    private CollectionCreateResult handleCollAlreadyExists(CollectionRef collRef, boolean ifNotExists) {
        if (ifNotExists) {
            return new CollectionAlreadyExists();
        } else {
            return new CollectionIllegallyAlreadyExists();
        }
    }

}
