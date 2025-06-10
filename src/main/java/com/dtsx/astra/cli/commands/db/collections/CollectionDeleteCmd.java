package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.operations.collection.CollectionDeleteOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "delete",
    aliases = { "rm" }
)
public class CollectionDeleteCmd extends AbstractCollectionSpecificCmd {
    
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if collection does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    protected boolean ifExists;

    private CollectionDeleteOperation collectionDeleteOperation;

    @Override
    protected void prelude() {
        super.prelude();
        this.collectionDeleteOperation = new CollectionDeleteOperation(collectionGateway);
    }

    @Override
    public OutputAll execute() {
        val request = new CollectionDeleteOperation.CollectionDeleteRequest(collRef, ifExists);
        val result = collectionDeleteOperation.execute(request);

        return switch (result) {
            case CollectionDeleteOperation.CollectionDeleteResult.CollectionNotFound(var collectionRef) -> {
                yield OutputAll.message("Collection " + highlight(collectionRef) + " does not exist; nothing to delete");
            }
            case CollectionDeleteOperation.CollectionDeleteResult.CollectionDeleted(var collectionRef, var descriptor) -> {
                yield OutputAll.message(
                    "Collection %s has been deleted from keyspace %s".formatted(
                        highlight(collectionRef), 
                        highlight(keyspaceRef)
                    )
                );
            }
        };
    }
}
