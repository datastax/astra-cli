package com.dtsx.astra.cli.gateways.db.keyspace;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
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

    void createKeyspace(KeyspaceRef keyspaceRef) throws InternalKeyspaceAlreadyExistsException;

    void deleteKeyspace(KeyspaceRef keyspaceRef) throws InternalKeyspaceNotFoundException;

    @RequiredArgsConstructor
    class InternalKeyspaceAlreadyExistsException extends Exception {
        public final KeyspaceRef keyspaceRef;
    }

    @RequiredArgsConstructor
    class InternalKeyspaceNotFoundException extends Exception {
        public final KeyspaceRef keyspaceRef;
    }
}
