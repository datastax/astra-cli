package com.dtsx.astra.cli.gateways;

import com.datastax.astra.client.databases.Database;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

public interface APIProvider {
    static APIProvider mkDefault(String token, AstraEnvironment env) {
        return new APIProviderImpl(token, env, GlobalInfoCache.INSTANCE);
    }

    AstraOpsClient astraOpsClient();

    DbOpsClient dbOpsClient(DbRef dbRef);

    Database dataApiDatabase(KeyspaceRef ksRef);

    String restApiEndpoint(DbRef dbRef, AstraEnvironment env);
}
