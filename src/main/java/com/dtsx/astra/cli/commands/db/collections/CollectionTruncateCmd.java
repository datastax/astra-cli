package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionTruncateOperation;
import lombok.val;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.collection.CollectionTruncateOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "truncate-collection",
    description = "Atomically delete all documents in a Data API collection"
)
@Example(
    comment = "Truncate a collection in the default keyspace",
    command = "astra db truncate-collection my_db -c my_collection"
)
@Example(
    comment = "Truncate a collection in a specific keyspace",
    command = "astra db truncate-collection my_db -k my_keyspace -c my_collection"
)
public class CollectionTruncateCmd extends AbstractCollectionSpecificCmd<CollectionTruncateResult> {
    @Override
    public final OutputAll execute(CollectionTruncateResult result) {
        val message = switch (result) {
            case CollectionTruncated() -> handleCollectionTruncated();
            case CollectionNotFound() -> throwCollectionNotFound();
        };
        
        return OutputAll.message(trimIndent(message));
    }

    private String handleCollectionTruncated() {
        return """
          Collection %s has been truncated.

          All documents have been deleted from the collection.
        """.formatted(
            highlight($collRef)
        );
    }

    private String throwCollectionNotFound() {
        throw new AstraCliException(COLLECTION_NOT_FOUND, """
          @|bold,red Error: Collection '%s' does not exist in keyspace '%s' of database '%s'.|@

          %s
          %s
        """.formatted(
            $collRef.name(),
            $keyspaceRef.name(),
            $keyspaceRef.db(),
            renderComment("List existing collections:"),
            renderCommand("astra db list-collections %s -k %s".formatted($keyspaceRef.db(), $keyspaceRef.name()))
        ));
    }

    @Override
    protected Operation<CollectionTruncateResult> mkOperation() {
        return new CollectionTruncateOperation(collectionGateway, new CollectionTruncateRequest($collRef));
    }
}
