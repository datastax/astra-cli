package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.collection.CollectionDeleteOperation.*;

@Command(
    name = "delete-collection"
)
public class CollectionDeleteCmd extends AbstractCollectionSpecificCmd<CollectionDeleteResult> {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if collection does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Override
    public final OutputAll execute(CollectionDeleteResult result) {
        val message = switch (result) {
            case CollectionNotFound() -> "Collection " + highlight(collRef) + " does not exist; nothing to delete";
            case CollectionIllegallyNotFound() -> throw new CollectionNotFoundException(collRef);
            case CollectionDeleted() -> "Collection %s has been deleted from keyspace %s".formatted(highlight(collRef.name()), highlight(keyspaceRef));
        };
        
        return OutputAll.message(message);
    }

    @Override
    protected Operation<CollectionDeleteResult> mkOperation() {
        return new CollectionDeleteOperation(collectionGateway, new CollectionDeleteRequest(collRef, ifExists));
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
