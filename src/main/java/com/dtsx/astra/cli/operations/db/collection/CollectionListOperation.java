package com.dtsx.astra.cli.operations.db.collection;

import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor
public class CollectionListOperation {
    private final CollectionGateway collectionGateway;
    private final KeyspaceGateway ksGateway;

    public record CollectionListResult(
        String keyspace,
        List<CollectionDescriptor> collections
    ) {}

    public List<CollectionListResult> execute(KeyspaceRef keyspaceRef, boolean all) {
        val dbRef = keyspaceRef.db();

        val keyspaces = (all)
            ? ksGateway.findAllKeyspaces(dbRef).keyspaces()
            : List.of(keyspaceRef.name());

        return keyspaces.stream()
            .map(ks -> KeyspaceRef.mkUnsafe(dbRef, ks))
            .map(ref -> new CollectionListResult(
                ref.name(),
                collectionGateway.findAllCollections(ref)
            ))
            .toList();
    }
}
