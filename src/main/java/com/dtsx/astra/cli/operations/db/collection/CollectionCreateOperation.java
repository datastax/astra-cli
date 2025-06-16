package com.dtsx.astra.cli.operations.db.collection;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

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

    public sealed interface CollectionCreateResult {}
    public record CollectionAlreadyExists() implements CollectionCreateResult {}
    public record CollectionCreated() implements CollectionCreateResult {}

    public CollectionCreateResult execute(CollectionCreateRequest request) {
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

    private CollectionCreateResult handleCollAlreadyExists(CollectionRef collRef, boolean ifExists) {
        if (ifExists) {
            return new CollectionAlreadyExists();
        } else {
            throw new CollectionAlreadyExistsException(collRef);
        }
    }

    public static class CollectionAlreadyExistsException extends AstraCliException {
        public CollectionAlreadyExistsException(CollectionRef collectionRef) {
            super("""
              @|bold,red Error: Collection '%s' already exists in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see all existing collections in this database.
              - Pass the %s flag to skip this error if the collection already exists.
            """.formatted(
                collectionRef,
                collectionRef.db(),
                AstraColors.highlight("astra db list-collections " + collectionRef.db() + " --all"),
                AstraColors.highlight("--if-not-exists")
            ));
        }
    }
}
