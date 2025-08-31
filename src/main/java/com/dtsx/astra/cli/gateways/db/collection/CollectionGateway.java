package com.dtsx.astra.cli.gateways.db.collection;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.SomeGateway;

import java.util.List;
import java.util.Optional;

public interface CollectionGateway extends SomeGateway {
    Optional<CollectionDefinition> findOne(CollectionRef collRef);

    List<CollectionDescriptor> findAll(KeyspaceRef ksRef);

    long estimatedDocumentCount(CollectionRef collRef);

    CreationStatus<CollectionRef> create(
        CollectionRef collRef,
        Optional<Integer> dimension,
        Optional<String> metric,
        Optional<String> defaultId,
        Optional<String> embeddingProvider,
        Optional<String> embeddingModel,
        Optional<String> embeddingKey,
        List<String> indexingAllow,
        List<String> indexingDeny
    );

    DeletionStatus<CollectionRef> delete(CollectionRef collRef);

    DeletionStatus<CollectionRef> truncate(CollectionRef collRef);
}
