package com.dtsx.astra.cli.operations.db.collection;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class CollectionDeleteOperation {
    private final CollectionGateway collectionGateway;

    public record CollectionDeleteRequest(
        CollectionRef collectionRef,
        boolean ifExists
    ) {}

    public sealed interface CollectionDeleteResult {}
    public record CollectionNotFound() implements CollectionDeleteResult {}
    public record CollectionDeleted() implements CollectionDeleteResult {}

    public CollectionDeleteResult execute(CollectionDeleteRequest request) {
        val status = collectionGateway.deleteCollection(request.collectionRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleCollDeleted();
            case DeletionStatus.NotFound<?> _ -> handleCollNotFound(request.collectionRef, request.ifExists);
        };
    }

    private CollectionDeleteResult handleCollDeleted() {
        return new CollectionDeleted();
    }

    private CollectionDeleteResult handleCollNotFound(CollectionRef collRef, boolean ifExists) {
        if (ifExists) {
            return new CollectionNotFound();
        } else {
            throw new CollectionNotFoundException(collRef);
        }
    }

    public static class CollectionNotFoundException extends AstraCliException {
        public CollectionNotFoundException(CollectionRef collectionRef) {
            super("""
              @|bold,red Error: Collection '%s' does not exist in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see all existing collections in this database.
              - Pass the %s flag to skip this error if the keyspace doesn't exist.
            """.formatted(
                collectionRef,
                collectionRef.db(),
                AstraColors.highlight("astra db list-collections " + collectionRef.db() + " --all"),
                AstraColors.highlight("--if-exists")
            ));
        }
    }
}
