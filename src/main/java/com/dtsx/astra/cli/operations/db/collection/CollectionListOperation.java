package com.dtsx.astra.cli.operations.db.collection;

import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionListOperation.CollectionListResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CollectionListOperation implements Operation<Stream<CollectionListResult>> {
    private final CollectionGateway collectionGateway;
    private final KeyspaceGateway ksGateway;
    private final CollectionListRequest request;

    public record CollectionListResult(
        String keyspace,
        List<CollectionDescriptor> collections
    ) {}

    public record CollectionListRequest(KeyspaceRef keyspaceRef, boolean all) {}

    @Override
    public Stream<CollectionListResult> execute() {
        val dbRef = request.keyspaceRef.db();

        val keyspaces = (request.all)
            ? ksGateway.findAll(dbRef).keyspaces().stream()
            : Stream.of(request.keyspaceRef.name());

        return keyspaces
            .map(ks -> KeyspaceRef.mkUnsafe(dbRef, ks))
            .map(ref -> new CollectionListResult(
                ref.name(),
                collectionGateway.findAll(ref)
            ));
    }
}
