package com.dtsx.astra.cli.domain.db.collections;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.datastax.astra.client.collections.definition.CollectionIndexDefinition;
import com.datastax.astra.client.collections.definition.CollectionIndexTypes;
import com.datastax.astra.client.collections.definition.SimilarityMetric;
import com.datastax.astra.client.collections.definition.VectorDefinition;
import com.datastax.astra.client.collections.definition.VectorizeDefinition;
import com.dtsx.astra.cli.domain.APIProviderImpl;
import com.dtsx.astra.cli.domain.db.keyspaces.KeyspaceRef;
import com.dtsx.astra.cli.exceptions.db.CollectionNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CollectionDaoImpl implements CollectionDao {
    private final APIProviderImpl api;

    @Override
    public List<String> findAll(KeyspaceRef ksRef) {
        return api.dataApiDatabase(ksRef).listCollectionNames();
    }

    @Override
    public CollectionDescriptor findOne(CollectionRef collRef) {
        try {
            return api.dataApiDatabase(collRef).getCollection(collRef.name()).getDefinition();
        } catch (Exception e) {
            throw new CollectionNotFoundException(collRef);
        }
    }

    @Override
    public void create(
        CollectionRef collRef,
        Integer dimension,
        String metric,
        String defaultId,
        String embeddingProvider,
        String embeddingModel,
        String embeddingKey,
        List<String> indexingAllow,
        List<String> indexingDeny
    ) {
        var collectionDefBuilder = CollectionDefinition.builder()
            .name(collRef.name());

        if (defaultId != null) {
            collectionDefBuilder.defaultId(defaultId);
        }

        // Set up vector configuration
        var vectorDefBuilder = VectorDefinition.builder()
            .dimension(dimension)
            .metric(SimilarityMetric.valueOf(metric.toUpperCase()));

        // Add vectorize configuration if provided
        if (embeddingProvider != null || embeddingModel != null || embeddingKey != null) {
            var vectorizeBuilder = VectorizeDefinition.builder();
            if (embeddingProvider != null) {
                vectorizeBuilder.provider(embeddingProvider);
            }
            if (embeddingModel != null) {
                vectorizeBuilder.model(embeddingModel);
            }
            if (embeddingKey != null) {
                vectorizeBuilder.authentication(Map.of("providerKey", embeddingKey));
            }
            vectorDefBuilder.vectorize(vectorizeBuilder.build());
        }

        collectionDefBuilder.vector(vectorDefBuilder.build());

        // Set up indexing configuration
        if (indexingAllow != null || indexingDeny != null) {
            var indexDefBuilder = CollectionIndexDefinition.builder()
                .type(CollectionIndexTypes.DEFAULT);
            
            if (indexingAllow != null) {
                indexDefBuilder.allow(indexingAllow);
            }
            if (indexingDeny != null) {
                indexDefBuilder.deny(indexingDeny);
            }
            
            collectionDefBuilder.indexing(indexDefBuilder.build());
        }

        api.dataApiDatabase(collRef).createCollection(collectionDefBuilder.build());
    }

    @Override
    public void delete(CollectionRef collRef) {
        try {
            api.dataApiDatabase(collRef).dropCollection(collRef.name());
        } catch (Exception e) {
            throw new CollectionNotFoundException(collRef);
        }
    }
}
