package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.exceptions.db.CollectionNotFoundException;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.output.output.OutputAll;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.output.AstraColors.highlight;

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

    @Override
    public OutputAll execute() {
        try {
            val existingCollection = AstraLogger.loading("Checking if collection exists", (_) -> (
                collectionService.getCollection(collRef)
            ));
            
            collectionService.deleteCollection(collRef);
            
            return OutputAll.message(
                "Collection %s has been deleted from keyspace %s".formatted(
                    highlight(collRef), 
                    highlight(keyspaceRef)
                )
            );
        } catch (CollectionNotFoundException e) {
            if (ifExists) {
                return OutputAll.message("Collection " + highlight(collRef) + " does not exist; nothing to delete");
            } else {
                throw e;
            }
        }
    }
}
