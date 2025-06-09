package com.dtsx.astra.cli.domain.db.collections;

import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.domain.APIProvider;
import com.dtsx.astra.cli.domain.db.keyspaces.KeyspaceRef;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;

public interface CollectionService {
    static CollectionService mkDefault(String token, AstraEnvironment env) {
        return new CollectionServiceImpl(CollectionDao.mkDefault(token, env));
    }

    List<String> listCollections(KeyspaceRef ksRef);
    
    boolean collectionExists(CollectionRef collRef);
    
    CollectionDescriptor getCollection(CollectionRef collRef);
    
    void createCollection(
        CollectionRef collRef,
        Integer dimension,
        String metric,
        String defaultId,
        String embeddingProvider,
        String embeddingModel,
        String embeddingKey,
        List<String> indexingAllow,
        List<String> indexingDeny
    );
    
    void deleteCollection(CollectionRef collRef);
}
