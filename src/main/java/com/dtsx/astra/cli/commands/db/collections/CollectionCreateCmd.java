package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation;
import com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation.CollectionAlreadyExists;
import com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation.CollectionCreateRequest;
import com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation.CollectionCreated;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "create-collection"
)
public final class CollectionCreateCmd extends AbstractCollectionSpecificCmd {
    @Option(
        names = { "--if-not-exists" },
        description = { "Will create a new collection only if none with same name", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifNotExists;

    @ArgGroup(validate = false, heading = "%nCollection configuration options:%n")
    public CollectionCreationOptions collectionCreationOptions;

    public static class CollectionCreationOptions {
        @Option(
            names = { "-d", "--dimension" },
            paramLabel = "DIMENSION",
            description = "Dimension of the vector space for this collection",
            required = true
        )
        public Integer dimension;

        @Option(
            names = { "--default-id" },
            paramLabel = "DEFAULT_ID",
            description = "Default identifier to use for the collection"
        )
        public String defaultId;

        @Option(
            names = { "-m", "--metric" },
            paramLabel = "METRIC",
            description = "Distance metric to use for vector similarity searches"
        )
        public String metric;

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
    public VectorizeOptions vectorizeOptions;

    public static class VectorizeOptions {
        @Option(
            names = { "--embedding-provider" },
            paramLabel = "EMBEDDING_PROVIDER",
            description = "Using Vectorize, embedding provider to use"
        )
        public String embeddingProvider;

        @Option(
            names = { "--embedding-model" },
            paramLabel = "EMBEDDING_MODEL",
            description = "Using Vectorize, embedding model to use"
        )
        public String embeddingModel;

        @Option(
            names = { "--embedding-key" },
            paramLabel = "EMBEDDING_KEY",
            description = "Using Vectorize, embedding key used for shared secret"
        )
        public String embeddingKey;
    }

    @Override
    public OutputAll execute() {
        val operation = new CollectionCreateOperation(collectionGateway);

        val result = operation.execute(new CollectionCreateRequest(
            collRef,
            collectionCreationOptions.dimension,
            collectionCreationOptions.metric,
            collectionCreationOptions.defaultId,
            vectorizeOptions != null ? vectorizeOptions.embeddingProvider : null,
            vectorizeOptions != null ? vectorizeOptions.embeddingModel : null,
            vectorizeOptions != null ? vectorizeOptions.embeddingKey : null,
            collectionCreationOptions.indexingAllow,
            collectionCreationOptions.indexingDeny,
            ifNotExists
        ));

        return switch (result) {
            case CollectionAlreadyExists _ -> {
                yield OutputAll.message("Collection " + highlight(collRef) + " already exists");
            }
            case CollectionCreated _ -> {
                yield OutputAll.message("Collection %s has been created in keyspace %s".formatted(highlight(collRef.name()), highlight(collRef.keyspace())));
            }
        };
    }
}
