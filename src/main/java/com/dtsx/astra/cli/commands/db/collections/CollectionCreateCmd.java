package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.collection.CollectionCreateOperation.*;

@Command(
    name = "create-collection"
)
public class CollectionCreateCmd extends AbstractCollectionSpecificCmd<CollectionCreateResult> {
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
    public final OutputAll execute(CollectionCreateResult result) {
        val message = switch (result) {
            case CollectionAlreadyExists() -> "Collection " + highlight(collRef) + " already exists";
            case CollectionIllegallyAlreadyExists() -> throw new CollectionAlreadyExistsException(collRef);
            case CollectionCreated() -> "Collection %s has been created in keyspace %s".formatted(highlight(collRef.name()), highlight(collRef.keyspace()));
        };
        
        return OutputAll.message(message);
    }

    @Override
    protected Operation<CollectionCreateResult> mkOperation() {
        return new CollectionCreateOperation(collectionGateway, new CollectionCreateRequest(
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
    }

    public static class CollectionAlreadyExistsException extends AstraCliException {
        public CollectionAlreadyExistsException(CollectionRef collectionRef) {
            super("""
              @|bold,red Error: Collection '%s' already exists in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see all existing collections in this database.
              - Pass the %s flag to skip this error if the collection already exists.
            """.formatted(
                collectionRef,
                collectionRef.db(),
                AstraColors.highlight("astra db list-collections " + collectionRef.db() + " --all"),
                AstraColors.highlight("--if-not-exists")
            ));
        }
    }
}
