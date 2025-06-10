package com.dtsx.astra.cli.gateways.collection;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;

public interface CollectionGateway {
    static CollectionGateway mkDefault(String token, AstraEnvironment env) {
        return new CollectionGatewayImpl(token, env);
    }

    List<String> findAllCollections(KeyspaceRef ksRef);

    boolean collectionExists(CollectionRef collRef);

    CollectionDefinition findOneCollection(CollectionRef collRef);

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