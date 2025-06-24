package com.dtsx.astra.cli.operations.db.collection;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionDeleteOperation.CollectionDeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class CollectionDeleteOperation implements Operation<CollectionDeleteResult> {
    private final CollectionGateway collectionGateway;
    private final CollectionDeleteRequest request;

    public sealed interface CollectionDeleteResult {}
    public record CollectionNotFound() implements CollectionDeleteResult {}
    public record CollectionIllegallyNotFound() implements CollectionDeleteResult {}
    public record CollectionDeleted() implements CollectionDeleteResult {}

    public record CollectionDeleteRequest(CollectionRef collRef, boolean ifExists) {}

    @Override
    public CollectionDeleteResult execute() {
        val status = collectionGateway.deleteCollection(request.collRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleCollDeleted();
            case DeletionStatus.NotFound<?> _ -> handleCollNotFound(request.collRef, request.ifExists);
        };
    }

    private CollectionDeleteResult handleCollDeleted() {
        return new CollectionDeleted();
    }

    private CollectionDeleteResult handleCollNotFound(CollectionRef collRef, boolean ifExists) {
        if (ifExists) {
            return new CollectionNotFound();
        } else {
            return new CollectionIllegallyNotFound();
        }
    }

}
