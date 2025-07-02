package com.dtsx.astra.cli.operations.db.collection;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dtsx.astra.cli.operations.db.collection.CollectionDescribeOperation.*;

@RequiredArgsConstructor
public class CollectionDescribeOperation implements Operation<CollectionDescribeResult> {
    private final CollectionGateway collectionGateway;
    private final CollectionDescribeRequest request;

    public sealed interface CollectionDescribeResult {}
    public record CollectionNotFound() implements CollectionDescribeResult {}
    public record CollectionFound(CollectionInfo collectionInfo) implements CollectionDescribeResult {}

    public record CollectionDescribeRequest(CollectionRef collectionRef) {}

    public record CollectionInfo(
        String name,
        long estimatedCount,
        Optional<String> defaultIdType,
        Optional<IndexingInfo> indexing,
        Optional<VectorInfo> vector,
        CollectionDefinition raw
    ) {}

    public record IndexingInfo(
        List<String> allow,
        List<String> deny
    ) {}

    public record VectorInfo(
        int dimension,
        String metric,
        Optional<VectorizeInfo> vectorize
    ) {}

    public record VectorizeInfo(
        String provider,
        String modelName,
        Map<String, Object> authentication,
        Map<String, Object> parameters
    ) {}

    @Override
    public CollectionDescribeResult execute() {
        val collectionResult = collectionGateway.findOneCollection(request.collectionRef);
        
        if (collectionResult.isEmpty()) {
            return new CollectionNotFound();
        }
        
        val definition = collectionResult.get();
        val estimatedCount = collectionGateway.estimatedDocumentCount(request.collectionRef);
        
        return new CollectionFound(new CollectionInfo(
            request.collectionRef.name(),
            estimatedCount,
            extractDefaultIdType(definition),
            extractIndexingInfo(definition),
            extractVectorInfo(definition),
            definition
        ));
    }

    private Optional<String> extractDefaultIdType(CollectionDefinition definition) {
        if (definition.getDefaultId() != null && definition.getDefaultId().getType() != null) {
            return Optional.of(definition.getDefaultId().getType().name());
        }
        return Optional.empty();
    }

    private Optional<IndexingInfo> extractIndexingInfo(CollectionDefinition definition) {
        if (definition.getIndexing() != null) {
            val indexing = definition.getIndexing();
            return Optional.of(new IndexingInfo(
                indexing.getAllow(),
                indexing.getDeny()
            ));
        }
        return Optional.empty();
    }

    private Optional<VectorInfo> extractVectorInfo(CollectionDefinition definition) {
        if (definition.getVector() != null) {
            val vector = definition.getVector();
            val vectorizeInfo = extractVectorizeInfo(vector);
            
            return Optional.of(new VectorInfo(
                vector.getDimension(),
                vector.getMetric(),
                vectorizeInfo
            ));
        }
        return Optional.empty();
    }

    private Optional<VectorizeInfo> extractVectorizeInfo(VectorOptions vector) {
        if (vector.getService() != null) {
            val service = vector.getService();
            return Optional.of(new VectorizeInfo(
                service.getProvider(),
                service.getModelName(),
                service.getAuthentication() != null ? service.getAuthentication() : Map.of(),
                service.getParameters() != null ? service.getParameters() : Map.of()
            ));
        }
        return Optional.empty();
    }
}
