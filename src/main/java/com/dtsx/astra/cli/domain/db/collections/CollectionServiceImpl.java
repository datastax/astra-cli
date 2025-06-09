package com.dtsx.astra.cli.domain.db.collections;

import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.domain.db.keyspaces.KeyspaceRef;
import com.dtsx.astra.cli.exceptions.db.CollectionNotFoundException;
import com.dtsx.astra.cli.output.AstraLogger;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.dtsx.astra.cli.output.AstraColors.highlight;

@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {
    private final CollectionDao collectionDao;

    @Override
    public List<String> listCollections(KeyspaceRef ksRef) {
        return AstraLogger.loading("Listing collections for keyspace " + highlight(ksRef), (_) ->
            collectionDao.findAll(ksRef)
        );
    }

    @Override
    public boolean collectionExists(CollectionRef collRef) {
        try {
            collectionDao.findOne(collRef);
            return true;
        } catch (CollectionNotFoundException e) {
            return false;
        }
    }

    @Override
    public CollectionDescriptor getCollection(CollectionRef collRef) {
        return AstraLogger.loading("Getting collection " + highlight(collRef), (_) ->
            collectionDao.findOne(collRef)
        );
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
            collectionDao.create(
                collRef,
                dimension,
                metric,
                defaultId,
                embeddingProvider,
                embeddingModel,
                embeddingKey,
                indexingAllow,
                indexingDeny
            );
            return null;
        });
    }

    @Override
    public void deleteCollection(CollectionRef collRef) {
        AstraLogger.loading("Deleting collection " + highlight(collRef), (_) -> {
            collectionDao.delete(collRef);
            return null;
        });
    }
}
