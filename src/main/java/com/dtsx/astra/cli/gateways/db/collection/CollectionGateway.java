package com.dtsx.astra.cli.gateways.db.collection;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

public interface CollectionGateway {
    static CollectionGateway mkDefault(String token, AstraEnvironment env) {
        return new CollectionGatewayImpl(token, env);
    }

    List<CollectionDescriptor> findAllCollections(KeyspaceRef ksRef);

    Optional<CollectionDefinition> findOneCollection(CollectionRef collRef);

    boolean collectionExists(CollectionRef collRef);

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
    ) throws InternalCollectionAlreadyExistsException;

    void deleteCollection(CollectionRef collRef) throws InternalCollectionNotFoundException;

    @RequiredArgsConstructor
    class InternalCollectionAlreadyExistsException extends Exception {
        public final CollectionRef collectionRef;
    }

    @RequiredArgsConstructor
    class InternalCollectionNotFoundException extends Exception {
        public final CollectionRef collectionRef;
    }
}
