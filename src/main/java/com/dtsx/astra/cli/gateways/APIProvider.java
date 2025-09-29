package com.dtsx.astra.cli.gateways;

import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.databases.Database;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface APIProvider {
    static APIProvider mkDefault(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new APIProviderImpl(ctx, token, env, GlobalInfoCache.INSTANCE);
    }

    AstraOpsClient astraOpsClient();

    DbOpsClient dbOpsClient(DbRef dbRef);

    Database dataApiDatabase(KeyspaceRef ksRef);

    DatabaseAdmin dataApiDatabaseAdmin(DbRef dbRef);

    String restApiEndpoint(DbRef dbRef, AstraEnvironment env);

    // I don't love having it here, but it's to avoid code duplication and circular dependencies
    Optional<com.dtsx.astra.sdk.db.domain.Database> tryResolveDb(@NotNull DbRef ref);
}
