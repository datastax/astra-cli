package com.dtsx.astra.cli.gateways.keyspace;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface KeyspaceGateway {
    static KeyspaceGateway mkDefault(String token, AstraEnvironment env) {
        return new KeyspaceGatewayImpl(APIProvider.mkDefault(token, env));
    }

    record FoundKeyspaces(
        @Nullable String defaultKeyspace,
        List<String> keyspaces
    ) {}

    FoundKeyspaces findAllKeyspaces(DbRef dbRef);

    boolean keyspaceExists(KeyspaceRef keyspaceRef);

    void createKeyspace(KeyspaceRef keyspaceRef);

    void deleteKeyspace(KeyspaceRef keyspaceRef);
}