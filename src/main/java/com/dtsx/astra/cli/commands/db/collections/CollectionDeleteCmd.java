package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.collection.CollectionDeleteOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "delete-collection",
    description = "Delete an existing Data API collection from the specified database and keyspace"
)
@Example(
    comment = "Delete a collection",
    command = "astra db delete-collection my_db -c my_collection"
)
@Example(
    comment = "Delete a collection from a non-default keyspace",
    command = "astra db delete-collection my_db -k my_keyspace -c my_collection"
)
@Example(
    comment = "Delete a collection without failing if it doesn't exist",
    command = "astra db delete-collection my_db -c my_collection --if-exists"
)
public class CollectionDeleteCmd extends AbstractCollectionSpecificCmd<CollectionDeleteResult> {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if collection does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Override
    public final OutputAll execute(CollectionDeleteResult result) {
        val message = switch (result) {
            case CollectionNotFound() -> handleCollectionNotFound();
            case CollectionIllegallyNotFound() -> throwCollectionNotFound();
            case CollectionDeleted() -> handleCollectionDeleted();
        };
        
        return OutputAll.message(trimIndent(message));
    }

    private String handleCollectionNotFound() {
        return """
          Collection %s does not exist in keyspace %s of database %s; nothing to delete.

          %s
          %s
        """.formatted(
            highlight($collRef.name()),
            highlight($keyspaceRef.name()),
            highlight($keyspaceRef.db()),
            renderComment("See all existing collections in the database:"),
            renderCommand("astra db list-collections %s --all".formatted($keyspaceRef.db()))
        );
    }

    private String handleCollectionDeleted() {
        return """
          Collection %s has been deleted from keyspace %s in database %s.
        """.formatted(
            highlight($collRef.name()),
            highlight($keyspaceRef.name()),
            highlight($keyspaceRef.db())
        );
    }

    private String throwCollectionNotFound() {
        throw new AstraCliException(COLLECTION_NOT_FOUND, """
          @|bold,red Error: Collection '%s' does not exist in keyspace '%s' of database '%s'.|@
        
          To ignore this error, provide the %s flag to skip this error if the collection doesn't exist.
        
          %s
          %s
        
          %s
          %s
        """.formatted(
            $collRef.name(),
            $keyspaceRef.name(),
            $keyspaceRef.db(),
            highlight("--if-exists"),
            renderComment("Example fix:"),
            renderCommand(originalArgs(), "--if-exists"),
            renderComment("See all existing collections in the database:"),
            renderCommand("astra db list-collections %s --all".formatted($keyspaceRef.db()))
        ));
    }

    @Override
    protected Operation<CollectionDeleteResult> mkOperation() {
        return new CollectionDeleteOperation(collectionGateway, new CollectionDeleteRequest($collRef, $ifExists));
    }
}
