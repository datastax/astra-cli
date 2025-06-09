package com.dtsx.astra.cli.domain.db.collections;

import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.domain.APIProvider;
import com.dtsx.astra.cli.domain.db.keyspaces.KeyspaceRef;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;

public interface CollectionDao {
    static CollectionDao mkDefault(String token, AstraEnvironment env) {
        return new CollectionDaoImpl(APIProvider.mkDefault(token, env));
    }

    List<String> findAll(KeyspaceRef ref);
    
    CollectionDescriptor findOne(CollectionRef collRef);
    
    void create(
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
    
    void delete(CollectionRef collRef);
}
