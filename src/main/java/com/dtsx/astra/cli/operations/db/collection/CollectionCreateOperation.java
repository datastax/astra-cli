package com.dtsx.astra.cli.operations.db.collection;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation.CollectionCreateResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CollectionCreateOperation implements Operation<CollectionCreateResult> {
    private final CollectionGateway collectionGateway;
    private final CollectionCreateRequest request;

    public record CollectionCreateRequest(
        CollectionRef collectionRef,
        Optional<Integer> dimension,
        Optional<String> metric,
        Optional<String> defaultId,
        Optional<String> embeddingProvider,
        Optional<String> embeddingModel,
        Optional<String> embeddingKey,
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
        val status = collectionGateway.create(
            request.collectionRef,
            request.dimension,
            request.metric,
            request.defaultId,
            request.embeddingProvider,
            request.embeddingModel,
            request.embeddingKey,
            request.indexingAllow,
            request.indexingDeny
        );

        return switch (status) {
            case CreationStatus.Created<?> _ -> handleCollCreated();
            case CreationStatus.AlreadyExists<?> _ -> handleCollAlreadyExists(request.ifNotExists);
        };
    }

    private CollectionCreateResult handleCollCreated() {
        return new CollectionCreated();
    }

    private CollectionCreateResult handleCollAlreadyExists(boolean ifNotExists) {
        if (ifNotExists) {
            return new CollectionAlreadyExists();
        } else {
            return new CollectionIllegallyAlreadyExists();
        }
    }
}
