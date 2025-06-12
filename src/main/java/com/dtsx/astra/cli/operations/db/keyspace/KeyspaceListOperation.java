package com.dtsx.astra.cli.operations.db.keyspace;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@RequiredArgsConstructor
public class KeyspaceListOperation {
    private final KeyspaceGateway keyspaceGateway;

    public record KeyspaceInfo(
        String name,
        boolean isDefault
    ) {}

    public List<KeyspaceInfo> execute(DbRef dbRef) {
        val result = keyspaceGateway.findAllKeyspaces(dbRef);

        return result.keyspaces().stream()
            .map(ks -> new KeyspaceInfo(ks, ks.equals(result.defaultKeyspace())))
            .toList();
    }
}
