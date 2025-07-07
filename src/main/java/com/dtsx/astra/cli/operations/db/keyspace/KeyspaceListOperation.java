package com.dtsx.astra.cli.operations.db.keyspace;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

import static com.dtsx.astra.cli.operations.db.keyspace.KeyspaceListOperation.*;

@RequiredArgsConstructor
public class KeyspaceListOperation implements Operation<List<KeyspaceInfo>> {
    private final KeyspaceGateway keyspaceGateway;
    private final KeyspaceListRequest request;

    public record KeyspaceInfo(
        String name,
        boolean isDefault
    ) {}

    public record KeyspaceListRequest(DbRef dbRef) {}

    @Override
    public List<KeyspaceInfo> execute() {
        val result = keyspaceGateway.findAll(request.dbRef);

        return result.keyspaces().stream()
            .map(ks -> new KeyspaceInfo(ks, ks.equals(result.defaultKeyspace())))
            .toList();
    }
}
