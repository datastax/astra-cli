package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.operations.collection.CollectionCreateOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "create"
)
public class CollectionCreateCmd extends AbstractCollectionSpecificCmd {
    
    @Option(
        names = { "--if-not-exists" },
        description = { "Will create a new collection only if none with same name", DEFAULT_VALUE },
        defaultValue = "false"
    )
    protected boolean ifNotExists;

    @ArgGroup(validate = false, heading = "%nCollection configuration options:%n")
    private CollectionCreationOptions collectionCreationOptions;

    static class CollectionCreationOptions {
        @Option(
            names = { "-d", "--dimension" },
            paramLabel = "DIMENSION",
            description = "Dimension of the vector space for this collection",
            required = true
        )
        protected Integer dimension;

        @Option(
            names = { "--default-id" },
            paramLabel = "DEFAULT_ID",
            description = "Default identifier to use for the collection"
        )
        protected String defaultId;

        @Option(
            names = { "-m", "--metric" },
            paramLabel = "METRIC",
            description = "Distance metric to use for vector similarity searches"
        )
        protected String metric;

        @Option(
            names = { "--indexing-allow" },
            paramLabel = "INDEXING_ALLOW",
            description = "List of attributes to add into index (comma separated)",
            split = ","
        )
        protected List<String> indexingAllow;

        @Option(
            names = { "--indexing-deny" },
            paramLabel = "INDEXING_DENY",
            description = "List of attributes to remove from index (comma separated)",
            split = ","
        )
        protected List<String> indexingDeny;
    }

    @ArgGroup(validate = false, heading = "%nVectorize options:%n")
    private VectorizeOptions vectorizeOptions;

    static class VectorizeOptions {
        @Option(
            names = { "--embedding-provider" },
            paramLabel = "EMBEDDING_PROVIDER",
            description = "Using Vectorize, embedding provider to use"
        )
        protected String embeddingProvider;

        @Option(
            names = { "--embedding-model" },
            paramLabel = "EMBEDDING_MODEL",
            description = "Using Vectorize, embedding model to use"
        )
        protected String embeddingModel;

        @Option(
            names = { "--embedding-key" },
            paramLabel = "EMBEDDING_KEY",
            description = "Using Vectorize, embedding key used for shared secret"
        )
        protected String embeddingKey;
    }

    private CollectionCreateOperation collectionCreateOperation;

    @Override
    protected void prelude() {
        super.prelude();
        this.collectionCreateOperation = new CollectionCreateOperation(collectionGateway);
    }

    @Override
    public OutputAll execute() {

        val request = new CollectionCreateOperation.CollectionCreateRequest(
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
        );

        val result = collectionCreateOperation.execute(request);

        return switch (result) {
            case CollectionCreateOperation.CollectionCreateResult.CollectionAlreadyExists(var collectionRef) -> {
                yield OutputAll.message("Collection " + highlight(collectionRef) + " already exists");
            }
            case CollectionCreateOperation.CollectionCreateResult.CollectionCreated(var collectionRef) -> {
                yield OutputAll.message(
                    "Collection %s has been created in keyspace %s".formatted(
                        highlight(collectionRef),
                        highlight(collectionRef.keyspace())
                    )
                );
            }
        };
    }
}
