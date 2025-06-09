package com.dtsx.astra.cli.domain;

import com.datastax.astra.client.databases.Database;
import com.dtsx.astra.cli.domain.db.DbRef;
import com.dtsx.astra.cli.domain.db.keyspaces.KeyspaceRef;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.AstraDBOpsClient;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

public interface APIProvider {
    static APIProviderImpl mkDefault(String token, AstraEnvironment env) {
        return new APIProviderImpl(token, env, GlobalInfoCache.INSTANCE);
    }

    AstraOpsClient astraOpsClient();

    AstraDBOpsClient dbOpsClient();

    Database dataApiDatabase(KeyspaceRef ksRef);

    String restApiEndpoint(DbRef dbRef);
}
