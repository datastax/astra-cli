package com.dtsx.astra.cli.operations.db.collection;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class CollectionTruncateOperation {
    private final CollectionGateway collectionGateway;

    public void execute(CollectionRef collRef) {
        val status = collectionGateway.truncateCollection(collRef);

        if (status instanceof DeletionStatus.NotFound<?>) {
            throw new CollectionNotFoundException(collRef);
        }
    }

    public static class CollectionNotFoundException extends AstraCliException {
        public CollectionNotFoundException(CollectionRef collectionRef) {
            super("""
              @|bold,red Error: Collection '%s' does not exist in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see all existing collections in this database.
              - Pass the %s flag to skip this error if the collection doesn't exist.
            """.formatted(
                collectionRef,
                collectionRef.db(),
                AstraColors.highlight("astra db list-collections " + collectionRef.db() + " --all"),
                AstraColors.highlight("--if-exists")
            ));
        }
    }
}
