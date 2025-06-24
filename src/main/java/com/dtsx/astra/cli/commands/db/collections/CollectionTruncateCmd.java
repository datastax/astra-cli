package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionTruncateOperation;
import lombok.val;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.collection.CollectionTruncateOperation.*;

@Command(
    name = "truncate-collection"
)
public class CollectionTruncateCmd extends AbstractCollectionSpecificCmd<CollectionTruncateResult> {
    @Override
    public final OutputAll execute(CollectionTruncateResult result) {
        val message = switch (result) {
            case CollectionTruncated() -> "Collection %s has been truncated in keyspace %s".formatted(highlight(collRef.name()), highlight(keyspaceRef));
            case CollectionNotFound() -> throw new CollectionNotFoundException(collRef);
        };
        
        return OutputAll.message(message);
    }

    @Override
    protected Operation<CollectionTruncateResult> mkOperation() {
        return new CollectionTruncateOperation(collectionGateway, new CollectionTruncateRequest(collRef));
    }

    public static class CollectionNotFoundException extends AstraCliException {
        public CollectionNotFoundException(CollectionRef collectionRef) {
            super("""
              @|bold,red Error: Collection '%s' does not exist in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see all existing collections in this database.
            """.formatted(
                collectionRef,
                collectionRef.db(),
                AstraColors.highlight("astra db list-collections " + collectionRef.db() + " --all")
            ));
        }
    }
}
