package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class CollectionGatewayStub implements CollectionGateway {
    @Override
    public Optional<CollectionDefinition> findOne(CollectionRef collRef) {
        return methodIllegallyCalled();
    }

    @Override
    public List<CollectionDescriptor> findAll(KeyspaceRef ksRef) {
        return methodIllegallyCalled();
    }

    @Override
    public long estimatedDocumentCount(CollectionRef collRef) {
        return methodIllegallyCalled();
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
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<CollectionRef> delete(CollectionRef collRef) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<CollectionRef> truncate(CollectionRef collRef) {
        return methodIllegallyCalled();
    }
}
