package com.dtsx.astra.cli.operations.db.table;

import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor
public class TableListOperation {
    private final TableGateway tableGateway;
    private final KeyspaceGateway ksGateway;

    public record TableListResult(
        String keyspace,
        List<TableDescriptor> tables
    ) {}

    public List<TableListResult> execute(KeyspaceRef keyspaceRef, boolean all) {
        val dbRef = keyspaceRef.db();

        val keyspaces = (all)
            ? ksGateway.findAllKeyspaces(dbRef).keyspaces()
            : List.of(keyspaceRef.name());

        return keyspaces.stream()
            .map(ks -> KeyspaceRef.mkUnsafe(dbRef, ks))
            .map(ref -> new TableListResult(
                ref.name(),
                tableGateway.findAllTables(ref)
            ))
            .toList();
    }
}
