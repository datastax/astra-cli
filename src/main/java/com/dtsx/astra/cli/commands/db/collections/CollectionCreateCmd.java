package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.COLLECTION_ALREADY_EXISTS;
import static com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation.*;
import static com.dtsx.astra.cli.utils.Collectionutils.sequencedMapOf;

@Command(
    name = "create-collection",
    description = "Create a new Data API collection in the specified database and keyspace"
)
@Example(
    comment = "Create a basic collection",
    command = "${cli.name} db create-collection my_db -c my_collection"
)
@Example(
    comment = "Create a vector collection",
    command = "${cli.name} db create-collection my_db -c my_collection --dimension 2048"
)
@Example(
    comment = "Create a collection in a non-default keyspace",
    command = "${cli.name} db create-collection my_db -k my_keyspace -c my_collection"
)
@Example(
    comment = "Create a collection without failing if it already exists",
    command = "${cli.name} db create-collection my_db -c my_collection --if-not-exists"
)
public class CollectionCreateCmd extends AbstractCollectionSpecificCmd<CollectionCreateResult> {
    @Option(
        names = { "--if-not-exists" },
        description = "Will create a new collection only if none with same name",
        defaultValue = "false"
    )
    public boolean $ifNotExists;

    @ArgGroup(validate = false, heading = "%nCollection configuration options:%n")
    public CollectionCreationOptions $collectionCreationOptions;

    public static class CollectionCreationOptions {
        @Option(
            names = { "-d", "--dimension" },
            paramLabel = "DIMENSION",
            description = "Dimension of the vector space for this collection"
        )
        public Optional<Integer> dimension;

        @Option(
            names = { "--default-id" },
            paramLabel = "DEFAULT_ID",
            description = "Default identifier to use for the collection"
        )
        public Optional<String> defaultId;

        @Option(
            names = { "-m", "--metric" },
            paramLabel = "METRIC",
            description = "Distance metric to use for vector similarity searches"
        )
        public Optional<String> metric;

        @Option(
            names = { "--indexing-allow" },
            paramLabel = "INDEXING_ALLOW",
            description = "List of attributes to add into index (comma separated)",
            split = ","
        )
        public List<String> indexingAllow;

        @Option(
            names = { "--indexing-deny" },
            paramLabel = "INDEXING_DENY",
            description = "List of attributes to remove from index (comma separated)",
            split = ","
        )
        public List<String> indexingDeny;
    }

    @ArgGroup(validate = false, heading = "%nVectorize options:%n")
    public VectorizeOptions $vectorizeOptions;

    public static class VectorizeOptions {
        @Option(
            names = { "--embedding-provider" },
            paramLabel = "EMBEDDING_PROVIDER",
            description = "Using Vectorize, embedding provider to use"
        )
        public Optional<String> embeddingProvider;

        @Option(
            names = { "--embedding-model" },
            paramLabel = "EMBEDDING_MODEL",
            description = "Using Vectorize, embedding model to use"
        )
        public Optional<String> embeddingModel;

        @Option(
            names = { "--embedding-key" },
            paramLabel = "EMBEDDING_KEY",
            description = "Using Vectorize, embedding key used for shared secret"
        )
        public Optional<String> embeddingKey;
    }

    @Override
    public final OutputAll execute(Supplier<CollectionCreateResult> result) {
        return switch (result.get()) {
            case CollectionAlreadyExists() -> handleCollectionAlreadyExists();
            case CollectionIllegallyAlreadyExists() -> throwCollectionAlreadyExists();
            case CollectionCreated() -> handleCollectionCreated();
        };
    }

    private OutputAll handleCollectionAlreadyExists() {
        val message = "Collection %s already exists in keyspace %s of database %s.".formatted(
            ctx.highlight($collRef.name()),
            ctx.highlight($keyspaceRef.name()),
            ctx.highlight($keyspaceRef.db())
        );

        val data = mkData(false);

        return OutputAll.response(message, data, List.of(
            new Hint("Get information about the existing collection:", "${cli.name} db describe-collection %s -k %s -c %s".formatted($keyspaceRef.db(), $keyspaceRef.name(), $collRef.name()))
        ));
    }

    private OutputAll handleCollectionCreated() {
        val message = "Collection %s has been created in keyspace %s of database %s.".formatted(
            ctx.highlight($collRef.name()),
            ctx.highlight($keyspaceRef.name()),
            ctx.highlight($keyspaceRef.db())
        );

        val data = mkData(true);

        return OutputAll.response(message, data, List.of(
            new Hint("Get more information about the new collection:", "${cli.name} db describe-collection %s -k %s -c %s".formatted($keyspaceRef.db(), $keyspaceRef.name(), $collRef.name()))
        ));
    }

    private <T> T throwCollectionAlreadyExists() {
        throw new AstraCliException(COLLECTION_ALREADY_EXISTS, """
          @|bold,red Error: Collection '%s' already exists in keyspace '%s' of database '%s'.|@

          To ignore this error, provide the @'!--if-not-exists!@ flag to skip this error if the collection already exists.
        """.formatted(
            $collRef.name(),
            $keyspaceRef.name(),
            $keyspaceRef.db()
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-not-exists"),
            new Hint("Get information about the existing collection:", "${cli.name} db describe-collection %s -k %s -c %s".formatted($keyspaceRef.db(), $keyspaceRef.name(), $collRef.name()))
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasCreated) {
        return sequencedMapOf(
            "wasCreated", wasCreated
        );
    }

    @Override
    protected Operation<CollectionCreateResult> mkOperation() {
        return new CollectionCreateOperation(collectionGateway, new CollectionCreateRequest(
            $collRef,
            Optional.ofNullable($collectionCreationOptions).flatMap(o -> o.dimension),
            Optional.ofNullable($collectionCreationOptions).flatMap(o -> o.metric),
            Optional.ofNullable($collectionCreationOptions).flatMap(o -> o.defaultId),
            Optional.ofNullable($vectorizeOptions).flatMap(o -> o.embeddingProvider),
            Optional.ofNullable($vectorizeOptions).flatMap(o -> o.embeddingModel),
            Optional.ofNullable($vectorizeOptions).flatMap(o -> o.embeddingKey),
            Optional.ofNullable($collectionCreationOptions).map(o -> o.indexingAllow).orElseGet(List::of),
            Optional.ofNullable($collectionCreationOptions).map(o -> o.indexingDeny).orElseGet(List::of),
            $ifNotExists
        ));
    }
}
