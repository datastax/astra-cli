package com.dtsx.astra.cli.operations.db.collection;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionTruncateOperation.CollectionTruncateResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class CollectionTruncateOperation implements Operation<CollectionTruncateResult> {
    private final CollectionGateway collectionGateway;
    private final CollectionTruncateRequest request;

    public sealed interface CollectionTruncateResult {}
    public record CollectionTruncated() implements CollectionTruncateResult {}
    public record CollectionNotFound() implements CollectionTruncateResult {}

    public record CollectionTruncateRequest(CollectionRef collRef) {}

    @Override
    public CollectionTruncateResult execute() {
        val status = collectionGateway.truncateCollection(request.collRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> new CollectionTruncated();
            case DeletionStatus.NotFound<?> _ -> new CollectionNotFound();
        };
    }

}
