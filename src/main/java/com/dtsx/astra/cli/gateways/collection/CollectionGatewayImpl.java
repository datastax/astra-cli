package com.dtsx.astra.cli.gateways.collection;

import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.APIProviderImpl;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.exceptions.db.CollectionNotFoundException;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class CollectionGatewayImpl implements CollectionGateway {
    private final APIProviderImpl api;

    public CollectionGatewayImpl(String token, AstraEnvironment env) {
        this.api = (APIProviderImpl) APIProvider.mkDefault(token, env);
    }

    @Override
    public List<String> findAllCollections(KeyspaceRef ksRef) {
        return AstraLogger.loading("Listing collections for keyspace " + highlight(ksRef), (_) ->
            api.dataApiDatabase(ksRef).listCollectionNames()
        );
    }

    @Override
    public boolean collectionExists(CollectionRef collRef) {
        try {
            findOneCollection(collRef);
            return true;
        } catch (CollectionNotFoundException e) {
            return false;
        }
    }

    @Override
    public CollectionDefinition findOneCollection(CollectionRef collRef) {
        return AstraLogger.loading("Getting collection " + highlight(collRef), (_) -> {
            try {
                return api.dataApiDatabase(collRef.keyspace()).getCollection(collRef.name()).getDefinition();
            } catch (Exception e) {
                throw new CollectionNotFoundException(collRef);
            }
        });
    }

    @Override
    public void createCollection(
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
        AstraLogger.loading("Creating collection " + highlight(collRef), (_) -> {
//            var collectionDefBuilder = new CollectionDefinition();
//
//            if (defaultId != null) {
//                collectionDefBuilder.defaultId(CollectionDefaultIdTypes);
//            }
//
//            // Set up vector configuration
//            var vectorDefBuilder = VectorDefinition.builder()
//                .dimension(dimension)
//                .metric(SimilarityMetric.valueOf(metric.toUpperCase()));
//
//            // Add vectorize configuration if provided
//            if (embeddingProvider != null || embeddingModel != null || embeddingKey != null) {
//                var vectorizeBuilder = VectorizeDefinition.builder();
//                if (embeddingProvider != null) {
//                    vectorizeBuilder.provider(embeddingProvider);
//                }
//                if (embeddingModel != null) {
//                    vectorizeBuilder.model(embeddingModel);
//                }
//                if (embeddingKey != null) {
//                    vectorizeBuilder.authentication(Map.of("providerKey", embeddingKey));
//                }
//                vectorDefBuilder.vectorize(vectorizeBuilder.build());
//            }
//
//            collectionDefBuilder.vector(vectorDefBuilder.build());
//
//            // Set up indexing configuration
//            if (indexingAllow != null || indexingDeny != null) {
//                var indexDefBuilder = CollectionIndexDefinition.builder()
//                    .type(CollectionIndexTypes.DEFAULT);
//
//                if (indexingAllow != null) {
//                    indexDefBuilder.allow(indexingAllow);
//                }
//                if (indexingDeny != null) {
//                    indexDefBuilder.deny(indexingDeny);
//                }
//
//                collectionDefBuilder.indexing(indexDefBuilder.build());
//            }
//
//            api.dataApiDatabase(collRef).createCollection(collectionDefBuilder.build());
            return null;
        });
    }

    @Override
    public void deleteCollection(CollectionRef collRef) {
        AstraLogger.loading("Deleting collection " + highlight(collRef), (_) -> {
            try {
                api.dataApiDatabase(collRef.keyspace()).dropCollection(collRef.name());
                return null;
            } catch (Exception e) {
                throw new CollectionNotFoundException(collRef);
            }
        });
    }
}
