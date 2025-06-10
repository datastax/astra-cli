package com.dtsx.astra.cli.operations.keyspace;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.keyspace.KeyspaceGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeyspaceListOperation {
    private final KeyspaceGateway keyspaceGateway;

    public record KeyspaceListResult(
        KeyspaceGateway.FoundKeyspaces foundKeyspaces
    ) {}

    public KeyspaceListResult execute(DbRef dbRef) {
        return new KeyspaceListResult(keyspaceGateway.findAllKeyspaces(dbRef));
    }
}
