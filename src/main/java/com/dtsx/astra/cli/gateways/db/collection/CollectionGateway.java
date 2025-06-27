package com.dtsx.astra.cli.gateways.db.collection;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.Optional;

public interface CollectionGateway {
    static CollectionGateway mkDefault(Token token, AstraEnvironment env) {
        return new CollectionGatewayImpl(token, env);
    }

    List<CollectionDescriptor> findAllCollections(KeyspaceRef ksRef);

    Optional<CollectionDefinition> findOneCollection(CollectionRef collRef);

    long estimatedDocumentCount(CollectionRef collRef);

    boolean collectionExists(CollectionRef collRef);

    CreationStatus<CollectionRef> createCollection(
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

    DeletionStatus<CollectionRef> deleteCollection(CollectionRef collRef);

    DeletionStatus<CollectionRef> truncateCollection(CollectionRef collRef);
}
