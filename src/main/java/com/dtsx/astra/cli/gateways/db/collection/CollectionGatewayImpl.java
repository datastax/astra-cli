package com.dtsx.astra.cli.gateways.db.collection;

import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDefinition.IndexingOptions;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.AstraToken;
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

    public CollectionGatewayImpl(AstraToken token, AstraEnvironment env) {
        this.api = (APIProviderImpl) APIProvider.mkDefault(token, env);
    }

    @Override
    public List<CollectionDescriptor> findAll(KeyspaceRef ksRef) {
        return AstraLogger.loading("Listing collections for keyspace " + highlight(ksRef), (_) ->
            api.dataApiDatabase(ksRef).listCollections()
        );
    }

    @Override
    public Optional<CollectionDefinition> findOne(CollectionRef collRef) {
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
    public long estimatedDocumentCount(CollectionRef collRef) {
        return AstraLogger.loading("Estimating document count for collection " + highlight(collRef), (_) -> {
            return api.dataApiDatabase(collRef.keyspace()).getCollection(collRef.name()).estimatedDocumentCount();
        });
    }

    @Override
    public CreationStatus<CollectionRef> create(
        CollectionRef collRef,
        Optional<Integer> dimension,
        Optional<String> metric,
        Optional<String> defaultId,
        Optional<String> embeddingProvider,
        Optional<String> embeddingModel,
        Optional<String> embeddingKey,
        List<String> indexingAllow,
        List<String> indexingDeny
    ) {
        val exists = exists(collRef);

        if (exists) {
            return CreationStatus.alreadyExists(collRef);
        }

        var collDef = new CollectionDefinition();

        defaultId.ifPresent(s -> collDef.defaultId(CollectionDefaultIdTypes.fromValue(s)));
        dimension.ifPresent(collDef::vectorDimension);
        metric.ifPresent(m -> collDef.vectorSimilarity(SimilarityMetric.fromValue(m)));

        if (embeddingProvider.isPresent() && embeddingModel.isPresent() && embeddingKey.isPresent()) {
            collDef.vectorize(embeddingProvider.get(), embeddingModel.get(), embeddingKey.get());
        }

        collDef.indexing(new IndexingOptions()
            .allow(indexingAllow)
            .deny(indexingDeny));

        AstraLogger.loading("Creating collection " + highlight(collRef), (_) -> {
            api.dataApiDatabase(collRef.keyspace()).createCollection(collRef.name(), collDef);
            return null;
        });

        return CreationStatus.created(collRef);
    }

    @Override
    public DeletionStatus<CollectionRef> delete(CollectionRef collRef) {
        if (!exists(collRef)) {
            return DeletionStatus.notFound(collRef);
        }

        AstraLogger.loading("Deleting collection " + highlight(collRef), (_) -> {
            api.dataApiDatabase(collRef.keyspace()).dropCollection(collRef.name());
            return null;
        });

        return DeletionStatus.deleted(collRef);
    }

    @Override
    public DeletionStatus<CollectionRef> truncate(CollectionRef collRef) {
        if (!exists(collRef)) {
            return DeletionStatus.notFound(collRef);
        }

        AstraLogger.loading("Truncating collection " + highlight(collRef), (_) -> {
            api.dataApiDatabase(collRef.keyspace()).getCollection(collRef.name()).deleteAll();
            return null;
        });

        return DeletionStatus.deleted(collRef);
    }

    private boolean exists(CollectionRef collRef) {
        return AstraLogger.loading("Checking if collection " + highlight(collRef) + " exists", (_) -> {
            return findOne(collRef).isPresent();
        });
    }
}
