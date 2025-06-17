package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.collection.CollectionTruncateOperation;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "truncate-collection"
)
public final class CollectionTruncateCmd extends AbstractCollectionSpecificCmd {
    @Override
    public OutputAll execute() {
        new CollectionTruncateOperation(collectionGateway).execute(collRef);

        return OutputAll.message(
            "Collection %s has been deleted from keyspace %s".formatted(highlight(collRef.name()), highlight(keyspaceRef))
        );
    }
}
