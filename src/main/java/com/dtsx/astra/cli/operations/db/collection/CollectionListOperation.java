package com.dtsx.astra.cli.operations.db.collection;

import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionListOperation.CollectionListResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CollectionListOperation implements Operation<Stream<CollectionListResult>> {
    private final CollectionGateway collectionGateway;
    private final KeyspaceGateway ksGateway;
    private final CollectionListRequest request;

    public record CollectionListRequest(DbRef dbRef, Optional<KeyspaceRef> keyspaceRef) {}

    public record CollectionListResult(
        String keyspace,
        List<CollectionDescriptor> collections
    ) {}

    @Override
    public Stream<CollectionListResult> execute() {
        val keyspaces = (request.keyspaceRef().isEmpty())
            ? ksGateway.findAll(request.dbRef).keyspaces().stream()
            : Stream.of(request.keyspaceRef.get().name());

        return keyspaces
            .map(ks -> KeyspaceRef.mkUnsafe(request.dbRef, ks))
            .map(ref -> new CollectionListResult(
                ref.name(),
                collectionGateway.findAll(ref)
            ));
    }
}
