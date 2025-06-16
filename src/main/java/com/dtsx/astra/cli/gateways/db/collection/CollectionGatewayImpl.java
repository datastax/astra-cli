package com.dtsx.astra.cli.gateways.db.collection;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.APIProviderImpl;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class CollectionGatewayImpl implements CollectionGateway {
    private final APIProviderImpl api;

    public CollectionGatewayImpl(String token, AstraEnvironment env) {
        this.api = (APIProviderImpl) APIProvider.mkDefault(token, env);
    }

    @Override
    public List<CollectionDescriptor> findAllCollections(KeyspaceRef ksRef) {
        return AstraLogger.loading("Listing collections for keyspace " + highlight(ksRef), (_) ->
            api.dataApiDatabase(ksRef).listCollections()
        );
    }

    @Override
    public Optional<CollectionDefinition> findOneCollection(CollectionRef collRef) {
        try {
            return AstraLogger.loading("Getting collection " + highlight(collRef), (_) -> {
                return Optional.of(
                    api.dataApiDatabase(collRef.keyspace()).getCollection(collRef.name()).getDefinition()
                );
            });
        } catch (DataAPIException e) {
            if (e.getErrorCode().equals("COLLECTION_NOT_EXIST")) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public boolean collectionExists(CollectionRef collRef) {
        return AstraLogger.loading("Checking if collection " + highlight(collRef) + " exists", (_) -> {
            return findOneCollection(collRef).isPresent();
        });
    }

    @Override
    public CreationStatus<CollectionRef> createCollection(
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
        val exists = collectionExists(collRef);

        if (exists) {
            return CreationStatus.alreadyExists(collRef);
        }

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

        return CreationStatus.created(collRef);
    }

    @Override
    public DeletionStatus<CollectionRef> deleteCollection(CollectionRef collRef) {
        if (!collectionExists(collRef)) {
            return DeletionStatus.notFound(collRef);
        }

        AstraLogger.loading("Deleting collection " + highlight(collRef), (_) -> {
            api.dataApiDatabase(collRef.keyspace()).dropCollection(collRef.name());
            return null;
        });

        return DeletionStatus.deleted(collRef);
    }
}
