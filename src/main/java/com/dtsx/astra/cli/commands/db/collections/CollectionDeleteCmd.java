package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.operations.db.collection.CollectionDeleteOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.collection.CollectionDeleteOperation.*;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "delete-collection"
)
public final class CollectionDeleteCmd extends AbstractCollectionSpecificCmd {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if collection does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Override
    public OutputAll execute() {
        val result = new CollectionDeleteOperation(collectionGateway).execute(collRef, ifExists);

        return switch (result) {
            case CollectionNotFound() -> {
                yield OutputAll.message("Collection " + highlight(collRef) + " does not exist; nothing to delete");
            }
            case CollectionDeleted() -> {
                yield OutputAll.message("Collection %s has been deleted from keyspace %s".formatted(highlight(collRef.name()), highlight(keyspaceRef)));
            }
        };
    }
}
