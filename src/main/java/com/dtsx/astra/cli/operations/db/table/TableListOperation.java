package com.dtsx.astra.cli.operations.db.table;

import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableListOperation.TableListResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TableListOperation implements Operation<Stream<TableListResult>> {
    private final TableGateway tableGateway;
    private final KeyspaceGateway ksGateway;
    private final TableListRequest request;

    public record TableListResult(
        String keyspace,
        List<TableDescriptor> tables
    ) {}

    public record TableListRequest(DbRef dbRef, Optional<KeyspaceRef> keyspaceRef) {}

    @Override
    public Stream<TableListResult> execute() {
        val keyspaces = (request.keyspaceRef().isEmpty())
            ? ksGateway.findAll(request.dbRef).keyspaces().stream()
            : Stream.of(request.keyspaceRef.get().name());

        return keyspaces
            .map(ks -> KeyspaceRef.mkUnsafe(request.dbRef, ks))
            .map(ref -> new TableListResult(
                ref.name(),
                tableGateway.findAll(ref)
            ));
    }
}
