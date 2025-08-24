package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.output.ExitCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.operations.db.collection.CollectionDeleteOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "delete-collection",
    description = "Delete an existing Data API collection from the specified database and keyspace"
)
@Example(
    comment = "Delete a collection",
    command = "${cli.name} db delete-collection my_db -c my_collection"
)
@Example(
    comment = "Delete a collection from a non-default keyspace",
    command = "${cli.name} db delete-collection my_db -k my_keyspace -c my_collection"
)
@Example(
    comment = "Delete a collection without failing if it doesn't exist",
    command = "${cli.name} db delete-collection my_db -c my_collection --if-exists"
)
public class CollectionDeleteCmd extends AbstractCollectionSpecificCmd<CollectionDeleteResult> {
    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if collection does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Override
    public final OutputAll execute(Supplier<CollectionDeleteResult> result) {
        return switch (result.get()) {
            case CollectionNotFound() -> handleCollectionNotFound();
            case CollectionIllegallyNotFound() -> throwCollectionNotFound();
            case CollectionDeleted() -> handleCollectionDeleted();
        };
    }

    private OutputAll handleCollectionNotFound() {
        val message = "Collection %s does not exist in keyspace %s of database %s; nothing to delete.".formatted(
            highlight($collRef.name()),
            highlight($keyspaceRef.name()),
            highlight($keyspaceRef.db())
        );

        val data = mkData(false);

        return OutputAll.response(message, data, List.of(
            new Hint("See all existing collections in the database:",
                "${cli.name} db list-collections %s --all".formatted($keyspaceRef.db()))
        ));
    }

    private OutputAll handleCollectionDeleted() {
        val message = "Collection %s has been deleted from keyspace %s in database %s.".formatted(
            highlight($collRef.name()),
            highlight($keyspaceRef.name()),
            highlight($keyspaceRef.db())
        );

        val data = mkData(true);

        return OutputAll.response(message, data);
    }

    private <T> T throwCollectionNotFound() {
        throw new AstraCliException(COLLECTION_NOT_FOUND, """
          @|bold,red Error: Collection '%s' does not exist in keyspace '%s' of database '%s'.|@
        
          To ignore this error, provide the @!--if-exists!@ flag to skip this error if the collection doesn't exist.
        """.formatted(
            $collRef.name(),
            $keyspaceRef.name(),
            $keyspaceRef.db()
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("See all existing collections in the database:", "${cli.name} db list-collections %s --all".formatted($keyspaceRef.db()))
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasDeleted) {
        return sequencedMapOf(
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected Operation<CollectionDeleteResult> mkOperation() {
        return new CollectionDeleteOperation(collectionGateway, new CollectionDeleteRequest($collRef, $ifExists));
    }
}
