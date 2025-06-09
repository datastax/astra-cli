package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.output.output.OutputAll;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.output.AstraColors.highlight;

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

    @Override
    public OutputAll execute() {
        // Validate that indexing-allow and indexing-deny are mutually exclusive
        if (collectionCreationOptions.indexingAllow != null && collectionCreationOptions.indexingDeny != null) {
            throw new OptionValidationException("indexing options", "indexing-allow and indexing-deny are mutually exclusive");
        }

        if (collectionService.collectionExists(collRef)) {
            if (ifNotExists) {
                return OutputAll.message("Collection " + highlight(collRef) + " already exists");
            } else {
                throw new OptionValidationException("collection", "Collection '%s' already exists. Use --if-not-exists to ignore this error".formatted(collRef.name()));
            }
        }

        // Create the collection with the specified options
        collectionService.createCollection(
            collRef,
            collectionCreationOptions.dimension,
            Optional.ofNullable(collectionCreationOptions.metric).orElse("cosine"),
            collectionCreationOptions.defaultId,
            vectorizeOptions != null ? vectorizeOptions.embeddingProvider : null,
            vectorizeOptions != null ? vectorizeOptions.embeddingModel : null,
            vectorizeOptions != null ? vectorizeOptions.embeddingKey : null,
            collectionCreationOptions.indexingAllow,
            collectionCreationOptions.indexingDeny
        );

        return OutputAll.message(
            "Collection %s has been created in keyspace %s".formatted(
                highlight(collRef),
                highlight(collRef.keyspace())
            )
        );
    }
}
